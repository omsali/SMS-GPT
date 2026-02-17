package com.aioversms.queryservice.sms.service;

import com.aioversms.queryservice.sms.dto.SmsRequestDto;
import com.aioversms.queryservice.sms.entity.SmsMessage;
import com.aioversms.queryservice.sms.repository.SmsMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final SmsMessageRepository repository;
    private final SmsEventProducer eventProducer;
    private final SessionService sessionService; // <--- NEW
    private final TwilioService twilioService;   // <--- NEW

    public void processIncomingSms(SmsRequestDto request) {
        log.info("Received SMS from {}: {}", request.getFrom(), request.getText());

        String incomingText = request.getText().trim();
        String phoneNumber = request.getFrom();

        // --- NEW LOGIC: Check for "MORE" keyword ---
        if (incomingText.equalsIgnoreCase("MORE")) {
            log.info("📝 User requested MORE pages.");
            
            String nextPage = sessionService.getNextPage(phoneNumber);
            
            if (nextPage != null) {
                // If we have a page in Redis, send it immediately
                twilioService.sendSms(phoneNumber, nextPage);
                log.info("✅ Sent cached page to {}", phoneNumber);
                return; // STOP HERE (Don't save to DB, don't send to AI)
            } else {
                // If they say MORE but nothing is left
                twilioService.sendSms(phoneNumber, "No more messages available.");
                return;
            }
        }
        // -------------------------------------------

        // Standard Flow (Save to DB & Send to AI)
        SmsMessage message = SmsMessage.builder()
                .phoneNumber(request.getFrom())
                .messageText(request.getText())
                .status("RECEIVED")
                .build();
        
        SmsMessage savedMessage = repository.save(message);
        log.info("Saved SMS to DB with ID: {}", savedMessage.getId());

        eventProducer.sendSmsEvent(savedMessage);
    }
}