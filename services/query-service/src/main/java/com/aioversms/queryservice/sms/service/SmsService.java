package com.aioversms.queryservice.sms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.aioversms.queryservice.sms.dto.SmsRequestDto;
import com.aioversms.queryservice.sms.entity.SmsMessage;
import com.aioversms.queryservice.sms.repository.SmsMessageRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final SmsMessageRepository repository;
    private final SmsEventProducer eventProducer;

    public void processIncomingSms(SmsRequestDto request) {
        log.info("Received SMS from {}: {}", request.getFrom(), request.getText());

        // 1. Save to Database
        SmsMessage message = SmsMessage.builder()
                .phoneNumber(request.getFrom())
                .messageText(request.getText())
                .status("RECEIVED")
                .build();

        if (message != null) {
            SmsMessage savedMessage = repository.save(message);
            log.info("Saved SMS to DB with ID: {}", savedMessage.getId());
            eventProducer.sendSmsEvent(savedMessage);
        }

        // TODO: In the next step, we will push this ID to Kafka!
    }
}