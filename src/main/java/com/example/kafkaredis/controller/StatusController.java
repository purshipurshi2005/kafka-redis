package com.example.kafkaredis.controller;

import com.example.kafkaredis.model.StatusEnum;
import com.example.kafkaredis.model.StatusEntity;
import com.example.kafkaredis.service.StatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/status")
@RequiredArgsConstructor
public class StatusController {
    private final StatusService statusService;

    @GetMapping("/{requestId}")
    public ResponseEntity<StatusEntity> getStatus(@PathVariable String requestId) {
        return statusService.getStatus(requestId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/correlation/{correlationId}")
    public ResponseEntity<StatusEntity> getStatusByCorrelationId(@PathVariable String correlationId) {
        return statusService.getStatusByCorrelationId(correlationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<StatusEntity>> getStatusesByStatus(@PathVariable StatusEnum status) {
        return ResponseEntity.ok(statusService.getStatusesByStatus(status));
    }

    @GetMapping("/by-source/{source}")
    public ResponseEntity<List<StatusEntity>> getStatusesBySource(@PathVariable String source) {
        return ResponseEntity.ok(statusService.getStatusesBySource(source));
    }

    @GetMapping("/expired")
    public ResponseEntity<List<StatusEntity>> getExpiredStatuses(
            @RequestParam(defaultValue = "24") int hoursAgo) {
        Instant before = Instant.now().minusSeconds(hoursAgo * 3600L);
        return ResponseEntity.ok(statusService.getExpiredStatuses(before));
    }

    @PostMapping
    public ResponseEntity<StatusEntity> createStatus(
            @RequestBody String payload,
            @RequestHeader(name = "X-Correlation-ID", required = false) String correlationId,
            @RequestHeader(name = "X-Source", required = false) String source,
            @RequestHeader(name = "X-Metadata", required = false) Map<String, String> metadata) {
        StatusEntity status = statusService.createStatus(payload, source, correlationId, metadata);
        return ResponseEntity.ok(status);
    }

    @PutMapping("/{requestId}/status")
    public ResponseEntity<StatusEntity> updateStatus(
            @PathVariable String requestId,
            @RequestParam StatusEnum status,
            @RequestParam(required = false) String errorMessage) {
        try {
            StatusEntity updatedStatus = statusService.updateStatus(requestId, status, errorMessage);
            return ResponseEntity.ok(updatedStatus);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> deleteStatus(@PathVariable String requestId) {
        statusService.deleteStatus(requestId);
        return ResponseEntity.noContent().build();
    }
} 