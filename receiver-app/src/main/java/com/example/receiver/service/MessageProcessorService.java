package com.example.receiver.service;

import com.example.receiver.model.SharedMessage;
import com.example.receiver.repository.MessageRepository;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MessageProcessorService {

    private final HazelcastInstance hazelcastInstance;
    private final MessageRepository repository;

    public MessageProcessorService(HazelcastInstance hazelcastInstance,
            MessageRepository repository) {
        this.hazelcastInstance = hazelcastInstance;
        this.repository = repository;
    }

    @PostConstruct
    public void recoverPendingMessages() {
        log.info("Startup recovery: checking for PENDING messages in DB...");

        List<SharedMessage> pendingMessages = repository.findByStatus("PENDING");

        if (pendingMessages.isEmpty()) {
            log.info("No PENDING messages found. All caught up!");
            return;
        }

        log.info("Found {} PENDING messages. Processing...", pendingMessages.size());

        IMap<String, SharedMessage> messageMap = hazelcastInstance.getMap("shared-messages");

        for (SharedMessage message : pendingMessages) {
            processMessage(message, messageMap);
        }

        log.info("Startup recovery complete. Processed {} messages.", pendingMessages.size());
    }

    public void processMessage(SharedMessage message, IMap<String, SharedMessage> messageMap) {
        try {
            log.info("Processing message: id={}, content='{}'", message.getId(), message.getContent());

            message.setStatus("PROCESSING");
            message.setProcessedAt(LocalDateTime.now());

            // Simulate processing work
            Thread.sleep(100);

            message.setStatus("PROCESSED");
            message.setProcessedAt(LocalDateTime.now());

            // Update in Hazelcast (MapStore auto-syncs to DB)
            messageMap.put(message.getId(), message);

            log.info("Message processed: id={}", message.getId());

        } catch (Exception e) {
            log.error("Failed to process message: id={}", message.getId(), e);
            message.setStatus("FAILED");
            messageMap.put(message.getId(), message);
        }
    }
}
