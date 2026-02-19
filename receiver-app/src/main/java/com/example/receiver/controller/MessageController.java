package com.example.receiver.controller;

import com.example.common.model.SharedMessage;
import com.example.receiver.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageRepository repository;

    public MessageController(MessageRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/all")
    public ResponseEntity<List<SharedMessage>> getAllMessages() {
        List<SharedMessage> messages = repository.findAll();
        return ResponseEntity.ok(messages);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAll() {
        long count = repository.count();
        repository.deleteAll();
        log.info("Cleared all messages: {} deleted", count);
        Map<String, Object> result = new HashMap<>();
        result.put("cleared", count);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("app", "receiver-app");
        status.put("total", repository.count());
        status.put("processed", (long) repository.findByStatus("PROCESSED").size());
        status.put("pending", (long) repository.findByStatus("PENDING").size());
        status.put("failed", (long) repository.findByStatus("FAILED").size());
        return ResponseEntity.ok(status);
    }
}
