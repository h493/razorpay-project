package com.himanshu.razorpay.api_gateway_service.security;

import com.himanshu.razorpay.common_library.exception.RateLimitException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class GatewayAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String BASIC_PREFIX = "Basic ";

    private final JwtAuthHandler jwtAuthHandler;
    private final ApiKeyAuthHandler apiKeyAuthHandler;
    private final PublicRouteMatcher publicRouteMatcher;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.info("Incoming Request : {}", request.getRequestURI());

        if(publicRouteMatcher.isPublic(request.getRequestURI())){
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        try {
            Map<String, String> identityHeaders = Map.of();
            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                identityHeaders = apiKeyAuthHandler.authentication(authHeader, response);
            } else if (authHeader != null && authHeader.startsWith(BASIC_PREFIX)) {
                identityHeaders = jwtAuthHandler.authenticate(authHeader.substring(BASIC_PREFIX.length()));
            }else{
                reject(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid Authorization header");
                return;
            }

            HeaderAugmentingRequestWrapper wrapped = new HeaderAugmentingRequestWrapper(request);
            identityHeaders.forEach((key, value) -> wrapped.putHeader(key, value));

            filterChain.doFilter(wrapped, response);
        }catch (RateLimitException e){
            response.setHeader("Retry-After", String.valueOf(e.getRetryAfterSeconds()));
            reject(response, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", e.getMessage());
        }catch (GatewayAuthenticationException e){
            reject(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", e.getMessage());
        }catch (Exception e){
            log.warn("Gateway auth failed for path ={}", request.getRequestURI(), e);
            reject(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "INVALID Credentials");
        }
    }

    private void reject(HttpServletResponse response, HttpStatus status, String errorCode, String message)
    throws IOException{
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Map.of("errorCode", errorCode, "errorDescription", message));
    }
}
