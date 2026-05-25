package com.rafasterch.youtube.downloader.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final ConcurrentHashMap<String, LinkedList<Long>> requestLog = new ConcurrentHashMap<>();

    private final int maxRequests;

    private static final long WINDOW_MS = 60_000L;

    public RateLimitInterceptor(
            @Value("${app.rate-limit.requests-per-minute:10}") int maxRequests) {
        this.maxRequests = maxRequests;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {

        String clientIp = resolveClientIp(request);
        long now = System.currentTimeMillis();

        int[] currentSize = {0};

        requestLog.compute(clientIp, (ip, timestamps) -> {
            if (timestamps == null) {
                timestamps = new LinkedList<>();
            }
            long cutoff = now - WINDOW_MS;
            while (!timestamps.isEmpty() && timestamps.getFirst() < cutoff) {
                timestamps.removeFirst();
            }
            timestamps.addLast(now);
            currentSize[0] = timestamps.size();
            return timestamps;
        });

        if (currentSize[0] > maxRequests) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Too many requests\"}");
            return false;
        }

        return true;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String[] parts = forwarded.split(",");
            // Rightmost entry is added by the trusted proxy — cannot be spoofed by client
            return parts[parts.length - 1].trim();
        }
        return request.getRemoteAddr();
    }
}
