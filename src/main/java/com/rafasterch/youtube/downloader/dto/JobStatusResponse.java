package com.rafasterch.youtube.downloader.dto;

import java.time.Instant;
import java.util.UUID;

public record JobStatusResponse(
        UUID id,
        String url,
        String format,
        String status,
        String fileName,
        String signedUrl,
        Instant signedUrlExpiresAt,
        String errorMessage,
        Instant createdAt,
        Instant updatedAt
) {}
