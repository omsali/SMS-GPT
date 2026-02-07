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
    private final SmsPaginator paginator;     // <--- NEW
    private final SessionService sessionService; // <--- NEW
    // private final TwilioService twilioService; // <--- We will uncomment this next!

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

                // 2. Save subsequent pages to Redis (if any)
                sessionService.savePages(phoneNumber, pages);

                // 3. Send Page 1 Immediately
                String firstPage = pages.get(0);
                
                // --- TWILIO INTEGRATION POINT ---
                // twilioService.sendSms(new SmsRequest(phoneNumber, firstPage)); 
                
                // For now, Log it to verify Pagination Logic
                log.info("🚀 SENDING SMS PART 1 TO [{}]: {}", phoneNumber, firstPage);
                
                if (pages.size() > 1) {
                    log.info("⚠️ Message was long! {} more pages saved to Redis.", pages.size() - 1);
                }

                sms.setStatus("DELIVERED");
                repository.save(sms);
            }
        } catch (Exception e) {
            log.error("Error processing delivery: {}", e.getMessage());
        }
    }
}