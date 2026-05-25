package com.rafasterch.youtube.downloader.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisPublisherService {

    private static final Logger log = LoggerFactory.getLogger(RedisPublisherService.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${redis.queue.download-jobs:download:jobs}")
    private String queueKey;

    public void publish(UUID jobId) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of("jobId", jobId.toString()));
            redisTemplate.opsForList().rightPush(queueKey, payload);
            log.info("[JOB QUEUED] Job {} published to Redis queue", jobId);
        } catch (JsonProcessingException e) {
            log.error("[QUEUE ERROR] Failed to serialize job {} for Redis queue", jobId, e);
            throw new RuntimeException("Failed to publish job to queue", e);
        }
    }
}
