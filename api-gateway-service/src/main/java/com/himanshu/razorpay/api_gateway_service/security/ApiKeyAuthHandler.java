package com.himanshu.razorpay.api_gateway_service.security;

import com.himanshu.razorpay.api_gateway_service.client.ApiKeyLookupClient;
import com.himanshu.razorpay.common_library.cache.ApiKeyCache;
import com.himanshu.razorpay.common_library.cache.ApiKeyCacheEntry;
import com.himanshu.razorpay.common_library.exception.RateLimitException;
import com.himanshu.razorpay.common_library.ratelimit.RateLimitResult;
import com.himanshu.razorpay.common_library.ratelimit.RateLimiter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor

public class ApiKeyAuthHandler {
    public static final String BASIC_PREFIX = "Basic ";
    private final PasswordEncoder passwordEncoder;

    private final ApiKeyCache apiKeyCache;
    private final ApiKeyLookupClient apiKeyLookupClient;
    private final RateLimiter rateLimiter;


    @Value("${app.rate-limit.use-case.api-key.requests-per-minute:60}")
    private Integer maxRequestPerMinute;

    public Map<String, String> authentication(String header, HttpServletResponse response) {


            //Authorization : Basic key_asasd:sec_sdsadsa (base64)

            String[] credentials = decode(header);
            if (credentials == null) {
                throw new GatewayAuthenticationException("MalFound API Key Header");
            }

            String keyId = credentials[0];
            String rawSecret = credentials[1];

            ApiKeyCacheEntry apiKeyCacheEntry = apiKeyCache.get(keyId)
                    .orElseGet(() -> loadAndCache(keyId));


            if (apiKeyCacheEntry == null || !apiKeyCacheEntry.enabled() || !secretMatches(rawSecret, apiKeyCacheEntry)) {
                throw new GatewayAuthenticationException("Invalid or missing API Key");
            }

            RateLimitResult rateLimitResult = rateLimiter.check(keyId, maxRequestPerMinute, 60);

            if(!rateLimitResult.isAllowed()){
                throw new RateLimitException("Too Many request", rateLimitResult.retryAfterSeconds());
            }

            response.setHeader("X-RateLimit-Remaining", String.valueOf(rateLimitResult.remaining()));
            response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequestPerMinute));

            return Map.of(
                    "X-Merchant-Id", apiKeyCacheEntry.merchantId().toString(),
                    "X-Environment", apiKeyCacheEntry.environment().name(),
                    "X-Key-Id", apiKeyCacheEntry.keyId()
            );
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
       try{
           ApiKeyCacheEntry entry = apiKeyLookupClient.findByKeyId(keyId);
           apiKeyCache.put(keyId, entry);
           return entry;
       } catch (Exception e){
           log.warn("Api key lookup failed keyId = {}", keyId, e);
           return null;
       }
    }

    private String[] decode(String header){
        String encoded = header.substring(BASIC_PREFIX.length());
        String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);

        int colon = decoded.indexOf(":");
        if(colon < 1) return null;

        return new String[]{decoded.substring(0, colon), decoded.substring(colon+1)};
    }
}
