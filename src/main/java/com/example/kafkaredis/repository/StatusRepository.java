package com.example.kafkaredis.repository;

import com.example.kafkaredis.model.StatusEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatusRepository extends CrudRepository<StatusEntity, String> {
    List<StatusEntity> findByStatus(String status);
    List<StatusEntity> findByUpdatedAtBefore(Instant timestamp);
    Optional<StatusEntity> findByCorrelationId(String correlationId);
    List<StatusEntity> findBySource(String source);
} 