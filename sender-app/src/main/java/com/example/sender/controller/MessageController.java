package com.example.sender.controller;

import com.example.sender.model.SharedMessage;
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
        return ResponseEntity.ok(Map.of(
                "sent", count,
                "mapSize", senderService.getMapSize()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<SharedMessage>> getAllMessages() {
        List<SharedMessage> messages = repository.findAll();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "app", "sender-app",
                "mapSize", senderService.getMapSize()));
    }
}
