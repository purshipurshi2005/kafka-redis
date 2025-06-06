package com.example.kafkaredis.service;

import com.example.kafkaredis.events.v1.IncomingEvent;
import com.example.kafkaredis.model.StatusEnum;
import com.example.kafkaredis.model.StatusEntity;
import com.example.kafkaredis.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatusService {
    private final StatusRepository statusRepository;

    @Transactional
    public StatusEntity processStatusEvent(IncomingEvent event) {
        log.debug("Processing incoming event with requestId: {}", event.getRequestId());
        
        // Convert protobuf metadata map to Java map
        Map<String, String> metadata = new HashMap<>(event.getMetadataMap());
        
        // Create or update status entity
        StatusEntity entity = StatusEntity.builder()
                .requestId(event.getRequestId())
                .status(StatusEnum.RECEIVED.name())
                .updatedAt(Instant.ofEpochMilli(event.getCreatedAt()))
                .payload(event.getPayload())
                .source(event.getSource())
                .correlationId(event.getCorrelationId())
                .metadata(metadata)
                .schemaVersion(event.getSchemaVersion())
                .build();

        StatusEntity savedEntity = statusRepository.save(entity);
        log.info("Saved status entity for requestId: {} with status: {}", 
                savedEntity.getRequestId(), savedEntity.getStatus());
        
        return savedEntity;
    }

    @Transactional
    public StatusEntity createStatus(String payload, String source, String correlationId, Map<String, String> metadata) {
        StatusEntity entity = StatusEntity.builder()
                .requestId(UUID.randomUUID().toString())
                .status(StatusEnum.RECEIVED.name())
                .updatedAt(Instant.now())
                .payload(payload)
                .source(source)
                .correlationId(correlationId)
                .metadata(metadata)
                .schemaVersion(1)
                .build();

        return statusRepository.save(entity);
    }

    @Transactional
    public StatusEntity updateStatus(String requestId, StatusEnum status, String errorMessage) {
        return statusRepository.findById(requestId)
                .map(entity -> {
                    entity.setStatus(status.name());
                    entity.setUpdatedAt(Instant.now());
                    if (errorMessage != null) {
                        entity.setErrorMessage(errorMessage);
                    }
                    return statusRepository.save(entity);
                })
                .orElseThrow(() -> new IllegalArgumentException("Status not found for requestId: " + requestId));
    }

    public Optional<StatusEntity> getStatus(String requestId) {
        return statusRepository.findById(requestId);
    }

    public Optional<StatusEntity> getStatusByCorrelationId(String correlationId) {
        return statusRepository.findByCorrelationId(correlationId);
    }

    public List<StatusEntity> getStatusesByStatus(StatusEnum status) {
        return statusRepository.findByStatus(status.name());
    }

    public List<StatusEntity> getStatusesBySource(String source) {
        return statusRepository.findBySource(source);
    }

    public List<StatusEntity> getExpiredStatuses(Instant before) {
        return statusRepository.findByUpdatedAtBefore(before);
    }

    @Transactional
    public void deleteStatus(String requestId) {
        statusRepository.deleteById(requestId);
    }
} 