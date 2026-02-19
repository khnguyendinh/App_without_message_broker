package com.example.sender.service;

import com.example.common.model.SharedMessage;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class MessageSenderService {

    private final IMap<String, SharedMessage> messageMap;

    public MessageSenderService(HazelcastInstance hazelcastInstance) {
        this.messageMap = hazelcastInstance.getMap("shared-messages");
    }

    public SharedMessage sendMessage(String content) {
        String id = UUID.randomUUID().toString();

        SharedMessage message = SharedMessage.builder()
                .id(id)
                .content(content)
                .status("PENDING")
                .senderApp("sender-app")
                .createdAt(LocalDateTime.now())
                .build();

        messageMap.set(id, message);

        log.info("Message sent: id={}, content='{}'", id, content);
        return message;
    }

    public int getMapSize() {
        return messageMap.size();
    }

    public void clearMap() {
        messageMap.clear();
        log.info("Hazelcast map cleared");
    }
}
