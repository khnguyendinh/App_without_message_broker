package com.example.receiver.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "shared_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 2000)
    private String content;

    @Column(length = 20)
    private String status;

    @Column(name = "sender_app", length = 100)
    private String senderApp;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
