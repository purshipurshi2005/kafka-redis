package com.example.kafkaredis.consumer;

import com.example.kafkaredis.events.v1.IncomingEvent;
import com.example.kafkaredis.service.StatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatusConsumer {

    private final StatusService statusService;

    @KafkaListener(topics = "${app.kafka.topic.status}")
    public void handleStatusEvent(
            @Payload byte[] message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.debug("Received message from topic: {}, partition: {}, offset: {}, key: {}", 
                     topic, partition, offset, key);
            
            // Deserialize protobuf message
            IncomingEvent event = IncomingEvent.parseFrom(message);
            
            log.info("Processing status event: id={}, payload={}, timestamp={}", 
                    event.getRequestId(), event.getPayload(), event.getCreatedAt());
            
            // Process the event
            statusService.processStatusEvent(event);
            
            // Acknowledge the message
            acknowledgment.acknowledge();
            
            log.debug("Successfully processed and acknowledged message with key: {}", key);
            
        } catch (Exception e) {
            log.error("Error processing status event with key: {}, topic: {}, partition: {}, offset: {}", 
                     key, topic, partition, offset, e);
            
            // In a production environment, you might want to:
            // 1. Send to a dead letter topic
            // 2. Implement retry logic
            // 3. Store failed messages for manual processing
            
            // For now, we'll acknowledge to avoid infinite retries
            acknowledgment.acknowledge();
        }
    }
} 