package com.wallet.ewallet.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;

    public IdempotencyFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (!request.getRequestURI().contains("/transfer")) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getHeader("Idempotency-Key");

        if (key == null || key.isEmpty()) {
            response.setStatus(400);
            response.getWriter().write("Missing Idempotency-Key");
            return;
        }

        String redisKey = "idempotency:" + key;

        Boolean exists = redisTemplate.hasKey(redisKey);

        if (Boolean.TRUE.equals(exists)) {
            response.setStatus(409);
            response.getWriter().write("Duplicate request");
            return;
        }

        redisTemplate.opsForValue().set(redisKey, "processed", Duration.ofMinutes(10));

        filterChain.doFilter(request, response);
    }
}