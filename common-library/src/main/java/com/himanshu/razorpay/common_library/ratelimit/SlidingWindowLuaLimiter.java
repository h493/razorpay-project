package com.himanshu.razorpay.common_library.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;


@RequiredArgsConstructor
public class SlidingWindowLuaLimiter implements RateLimiter{

    /*
     * KEYS[1] = redis key
     * ARGV[1] = now in millis, ARGV[2] = window in millis
     * ARGV[3] = max requests allowed, ARGV[4] = unique member id
     *
     * returns { allowed, remaining, retryAfterSeconds }
     *
     * whole script runs as one atomic step, so prune -> count -> add cannot
     * interleave with another request the way the java version can
     */
    private static final String SCRIPT = """
            local key      = KEYS[1]
            local now      = tonumber(ARGV[1])
            local windowMs = tonumber(ARGV[2])
            local maxReq   = tonumber(ARGV[3])
            local member   = ARGV[4]

            redis.call('ZREMRANGEBYSCORE', key, '-inf', now - windowMs)

            local current = redis.call('ZCARD', key)

            if current >= maxReq then
                local oldest = redis.call('ZRANGE', key, 0, 0, 'WITHSCORES')
                local retryAfter = 1
                if oldest[2] then
                    local windowExpiresMs = tonumber(oldest[2]) + windowMs
                    retryAfter = math.ceil((windowExpiresMs - now) / 1000)
                    if retryAfter < 1 then retryAfter = 1 end
                end
                return { 0, 0, retryAfter }
            end

            redis.call('ZADD', key, now, member)
            redis.call('PEXPIRE', key, windowMs + 1000)

            return { 1, maxReq - current - 1, 0 }
            """;

    @SuppressWarnings("rawtypes")
    private static final RedisScript<List> SLIDING_WINDOW = new DefaultRedisScript<>(SCRIPT, List.class);

    private final StringRedisTemplate redis;

    @Override
    @SuppressWarnings("unchecked")
    public RateLimitResult check(String key, int maxRequestAllowed, long windowSeconds) {

        String redisKey = "ratelimit:sliding-lua:" + key;

        List<Long> result = redis.execute(
                SLIDING_WINDOW,
                List.of(redisKey),
                Long.toString(System.currentTimeMillis()),
                Long.toString(windowSeconds * 1000),
                Integer.toString(maxRequestAllowed),
                UUID.randomUUID().toString()
        );

        //fail open, redis being unreachable should not block traffic
        if(result == null || result.size() < 3){
            return RateLimitResult.allowed(maxRequestAllowed);
        }

        boolean allowed = result.get(0) == 1L;

        return allowed
                ? RateLimitResult.allowed(result.get(1).intValue())
                : RateLimitResult.denied(result.get(2).intValue());
    }
}
