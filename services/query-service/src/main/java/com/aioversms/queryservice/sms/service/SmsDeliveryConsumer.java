package com.aioversms.queryservice.sms.service;

import com.aioversms.queryservice.sms.entity.SmsMessage;
import com.aioversms.queryservice.sms.repository.SmsMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsDeliveryConsumer {

    private final SmsMessageRepository repository;
    private final SmsPaginator paginator;
    private final SessionService sessionService;
    private final TwilioService twilioService; // <--- INJECTED NOW

    @KafkaListener(topics = "answers.out", groupId = "sms-delivery-group")
    public void consumeAiAnswer(String payload) {
        log.info("📨 Received AI Answer: {}", payload);

        try {
            String[] parts = payload.split(":", 2);
            if (parts.length < 2) return;

            Long messageId = Long.parseLong(parts[0]);
            String aiResponse = parts[1];

            Optional<SmsMessage> originalMsg = repository.findById(messageId);
            if (originalMsg.isPresent()) {
                SmsMessage sms = originalMsg.get();
                String phoneNumber = sms.getPhoneNumber();

                // 1. Paginate
                List<String> pages = paginator.paginate(aiResponse);

                // 2. Save subsequent pages to Redis
                sessionService.savePages(phoneNumber, pages);

                // 3. Send Page 1 via Twilio
                String firstPage = pages.get(0);
                
                log.info("🚀 Sending SMS to provider...");
                twilioService.sendSms(phoneNumber, firstPage); // <--- REAL SEND
                
                sms.setStatus("DELIVERED");
                repository.save(sms);
            }
        } catch (Exception e) {
            log.error("Error processing delivery: {}", e.getMessage());
        }
    }
}
