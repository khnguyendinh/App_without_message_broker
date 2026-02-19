package com.example.receiver.listener;

import com.example.common.model.SharedMessage;
import com.example.receiver.service.MessageProcessorService;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageEntryListener implements EntryAddedListener<String, SharedMessage> {

    private final HazelcastInstance hazelcastInstance;
    private final MessageProcessorService processorService;

    public MessageEntryListener(HazelcastInstance hazelcastInstance,
            MessageProcessorService processorService) {
        this.hazelcastInstance = hazelcastInstance;
        this.processorService = processorService;
    }

    @PostConstruct
    public void registerListener() {
        IMap<String, SharedMessage> messageMap = hazelcastInstance.getMap("shared-messages");
        messageMap.addEntryListener(this, true);
        log.info("EntryListener registered on 'shared-messages' map");
    }

    @Override
    public void entryAdded(EntryEvent<String, SharedMessage> event) {
        SharedMessage message = event.getValue();

        if (!"PENDING".equals(message.getStatus())) {
            log.debug("Skipping non-PENDING message: id={}, status={}", message.getId(), message.getStatus());
            return;
        }

        log.info("New message detected: id={}, content='{}'",
                message.getId(), message.getContent());

        IMap<String, SharedMessage> messageMap = hazelcastInstance.getMap("shared-messages");
        processorService.processMessage(message, messageMap);
    }
}
