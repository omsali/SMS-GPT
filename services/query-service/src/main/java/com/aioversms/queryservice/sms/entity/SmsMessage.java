package com.aioversms.queryservice.sms.entity;

// package com.aioversms.queryservice.sms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "sms_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String messageText;

    @Column(nullable = false)
    private String status; // RECEIVED, PROCESSED, FAILED

    @CreationTimestamp
    @Column(name = "received_at", updatable = false)
    private LocalDateTime receivedAt;
}