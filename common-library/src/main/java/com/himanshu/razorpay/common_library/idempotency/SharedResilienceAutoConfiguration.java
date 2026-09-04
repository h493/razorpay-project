package com.himanshu.razorpay.common_library.idempotency;

import com.himanshu.razorpay.common_library.context.MerchantContext;
import com.himanshu.razorpay.common_library.ratelimit.FixedWindowRateLimiter;
import com.himanshu.razorpay.common_library.ratelimit.RateLimiter;
import com.himanshu.razorpay.common_library.ratelimit.SlidingWindowLuaLimiter;
import com.himanshu.razorpay.common_library.ratelimit.SlidingWindowRateLimiter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerExceptionResolver;

@AutoConfiguration
public class SharedResilienceAutoConfiguration {

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory){
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public IdempotencyStore idempotencyStore(StringRedisTemplate stringRedisTemplate){
        return new RedisIdempotencyStore(stringRedisTemplate);
    }

    @Bean
    public IdempotencyFilter idempotencyFilter(MerchantContext merchantContext,
                                               IdempotencyStore idempotencyStore,
                                               @Qualifier("handleExceptionResolver") HandlerExceptionResolver handlerExceptionResolver){
        return new IdempotencyFilter(merchantContext, idempotencyStore, handlerExceptionResolver);
    }

    @Bean
    @ConditionalOnProperty(name = "app.rate-limit.method", havingValue = "fixed")
    public RateLimiter fixedWindowRateLimiter(StringRedisTemplate stringRedisTemplate){
        return new FixedWindowRateLimiter(stringRedisTemplate);
    }

    @Bean
    @ConditionalOnProperty(name = "app.rate-limit.method", havingValue = "sliding")
    public RateLimiter slidingWindowRateLimiter(StringRedisTemplate stringRedisTemplate){
        return new SlidingWindowRateLimiter(stringRedisTemplate);
    }

    @Bean
    @ConditionalOnProperty(name = "app.rate-limit.method", havingValue = "sliding-lua")
    public RateLimiter slidingWindowLuaRateLimiter(StringRedisTemplate stringRedisTemplate){
        return new SlidingWindowLuaLimiter(stringRedisTemplate);
    }
}
