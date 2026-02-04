package com.aioversms.queryservice.sms.service;

import com.aioversms.queryservice.sms.entity.SmsMessage;
import com.aioversms.queryservice.sms.repository.SmsMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsDeliveryConsumer {

    private final SmsMessageRepository repository;

    @KafkaListener(topics = "answers.out", groupId = "sms-delivery-group")
    public void consumeAiAnswer(String payload) {
        log.info("📨 Received AI Answer from Kafka: {}", payload);

        try {
            // 1. Parse "ID:ANSWER"
            String[] parts = payload.split(":", 2);
            if (parts.length < 2) return;

            Long messageId = Long.parseLong(parts[0]);
            String aiResponse = parts[1];

            // 2. Fetch Original Message to get Phone Number
            Optional<SmsMessage> originalMsg = repository.findById(messageId);
            
            if (originalMsg.isPresent()) {
                SmsMessage sms = originalMsg.get();
                String phoneNumber = sms.getPhoneNumber();
                
                // 3. Simulate Sending SMS (The "Delivery")
                sendSmsToUser(phoneNumber, aiResponse);
                
                // 4. Update Status in DB
                sms.setStatus("DELIVERED");
                repository.save(sms);
            } else {
                log.error("❌ Original message ID {} not found", messageId);
            }

        } catch (Exception e) {
            log.error("Error processing AI answer: {}", e.getMessage());
        }
    }

    private void sendSmsToUser(String phoneNumber, String message) {
        // This is where we will hook up Twilio later.
        // For now, seeing this log means the loop is CLOSED.
        log.info("==================================================");
        log.info("🚀 SENDING SMS TO [{}]:", phoneNumber);
        log.info("📝 TEXT: {}", message);
        log.info("==================================================");
    }
}