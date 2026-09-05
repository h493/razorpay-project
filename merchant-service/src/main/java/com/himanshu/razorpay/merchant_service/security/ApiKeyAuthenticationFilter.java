package com.himanshu.razorpay.merchant_service.security;


import com.himanshu.razorpay.common_library.context.MerchantContext;
import com.himanshu.razorpay.common_library.exception.RateLimitException;
import com.himanshu.razorpay.common_library.ratelimit.RateLimitResult;
import com.himanshu.razorpay.common_library.ratelimit.RateLimiter;
import com.himanshu.razorpay.merchant_service.cache.ApiKeyCache;
import com.himanshu.razorpay.merchant_service.cache.ApiKeyCacheEntry;
import com.himanshu.razorpay.merchant_service.entity.ApiKey;
import com.himanshu.razorpay.merchant_service.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String BASIC_PREFIX = "Basic ";
    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;
    private final MerchantContext merchantContext;
    private final HandlerExceptionResolver handlerExceptionResolver;

    private final ApiKeyCache apiKeyCache;
    private final RateLimiter rateLimiter;


    @Value("${app.rate-limit.use-case.api-key.requests-per-minute:60}")
    private Integer maxRequestPerMinute;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.info("Incoming request: {}", request.getRequestURI());

        try {
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith(BASIC_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }

            //Authorization : Basic key_asasd:sec_sdsadsa (base64)

            String[] credentials = decode(header);
            if (credentials == null) {
                throw new BadRequestException("MalFound API Key Header");
            }

            String keyId = credentials[0];
            String rawSecret = credentials[1];

            ApiKeyCacheEntry apiKeyCacheEntry = apiKeyCache.get(keyId)
                    .orElseGet(() -> loadAndCache(keyId));


            if (apiKeyCacheEntry == null || !apiKeyCacheEntry.enabled() || !secretMatches(rawSecret, apiKeyCacheEntry)) {
                throw new BadRequestException("Invalid or missing API Key");
            }

            RateLimitResult rateLimitResult = rateLimiter.check(keyId, maxRequestPerMinute, 60);

            if(!rateLimitResult.isAllowed()){
                throw new RateLimitException("Too Many request", rateLimitResult.retryAfterSeconds());
            }

            response.setHeader("X-RateLimit-Remaining", String.valueOf(rateLimitResult.remaining()));
            response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequestPerMinute));

            var auth = new UsernamePasswordAuthenticationToken(keyId, null,
                    List.of(new SimpleGrantedAuthority("API_KEY_ROLE"))
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
            merchantContext.setMerchantId(apiKeyCacheEntry.merchantId());
            merchantContext.setKeyId(apiKeyCacheEntry.keyId());

            filterChain.doFilter(request, response);
        } catch (Exception e){
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    private boolean secretMatches(String rawSecret, ApiKeyCacheEntry apiKey){
            if (passwordEncoder.matches(rawSecret, apiKey.keySecretHash())) {
                return true;
            }

            boolean isInGracePeriod = apiKey.isInGracePeriod();


            return isInGracePeriod
                    && apiKey.previousKeySecretHash() != null
                    && passwordEncoder.matches(rawSecret, apiKey.previousKeySecretHash());
    }

    private ApiKeyCacheEntry loadAndCache(String keyId) {
        ApiKey apiKey = apiKeyRepository.findByKeyId(keyId).orElse(null);

        if(apiKey == null) return null;
        ApiKeyCacheEntry apiKeyCacheEntry = new ApiKeyCacheEntry(
                apiKey.getKeyId(), apiKey.getKeySecretHash(),
                apiKey.getPreviousKeySecretHash(), apiKey.getGracePeriodExpiresAt(),
                apiKey.getMerchant().getId(),
                apiKey.getEnvironment(),
                apiKey.isEnabled()
        );

        apiKeyCache.put(keyId, apiKeyCacheEntry);
        return apiKeyCacheEntry;
    }




    private String[] decode(String header){
        String encoded = header.substring(BASIC_PREFIX.length());
        String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);

        int colon = decoded.indexOf(":");
        if(colon < 1) return null;

        return new String[]{decoded.substring(0, colon), decoded.substring(colon+1)};
    }
}
