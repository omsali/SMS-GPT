package com.aioversms.queryservice.sms.service;

import com.aioversms.queryservice.sms.entity.SmsMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "queries.in";

    public void sendSmsEvent(SmsMessage message) {
        log.info("Publishing SMS ID {} to topic {}", message.getId(), TOPIC);
        
        // In a real production app, we would send a JSON object (DTO).
        // For Stage 1, we will send a simple string format: "ID:MESSAGE"
        String payload = message.getId() + ":" + message.getMessageText();
        String phoneNumber = message.getPhoneNumber() != null ? message.getPhoneNumber() : "default_value";
        
        kafkaTemplate.send(TOPIC, phoneNumber, payload);
    }
}