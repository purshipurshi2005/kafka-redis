package com.example.kafkaredis.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@RedisHash("status")
public class StatusEntity {
    @Id
    private String requestId;

    @Indexed
    private String status;

    @Indexed
    private Instant updatedAt;

    private String payload;
    private String errorMessage;
    private Map<String, String> metadata;
    private String source;
    private String correlationId;
    private Integer schemaVersion;
} 