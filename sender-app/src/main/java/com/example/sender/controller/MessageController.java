package com.example.sender.controller;

import com.example.common.model.SharedMessage;
import com.example.sender.repository.MessageRepository;
import com.example.sender.service.MessageSenderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageSenderService senderService;
    private final MessageRepository repository;

    public MessageController(MessageSenderService senderService, MessageRepository repository) {
        this.senderService = senderService;
        this.repository = repository;
    }

    @PostMapping("/send")
    public ResponseEntity<SharedMessage> sendMessage(@RequestParam String content) {
        SharedMessage message = senderService.sendMessage(content);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/send-batch")
    public ResponseEntity<Map<String, Object>> sendBatch(
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(defaultValue = "Message") String prefix) {
        for (int i = 1; i <= count; i++) {
            senderService.sendMessage(prefix + " #" + i);
        }
        java.util.HashMap<String, Object> result = new java.util.HashMap<>();
        result.put("sent", count);
        result.put("total", repository.count());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SharedMessage>> getAllMessages() {
        List<SharedMessage> messages = repository.findAll();
        return ResponseEntity.ok(messages);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAll() {
        long count = repository.count();
        senderService.clearMap();
        repository.deleteAll();
        log.info("Cleared all messages: {} deleted", count);
        java.util.HashMap<String, Object> result = new java.util.HashMap<>();
        result.put("cleared", count);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        java.util.HashMap<String, Object> status = new java.util.HashMap<>();
        status.put("app", "sender-app");
        status.put("total", repository.count());
        return ResponseEntity.ok(status);
    }
}
