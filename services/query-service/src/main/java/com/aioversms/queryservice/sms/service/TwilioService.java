package com.aioversms.queryservice.sms.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TwilioService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        // Initialize the Twilio Client once at startup
        Twilio.init(accountSid, authToken);
        log.info("Twilio initialized with Account SID: {}", accountSid);
    }

    public void sendSms(String to, String text) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(to),       // To
                    new PhoneNumber(fromNumber), // From
                    text                       // Body
            ).create();

            log.info("✅ SMS sent to {}! SID: {}", to, message.getSid());
        } catch (Exception e) {
            log.error("❌ Failed to send SMS to {}: {}", to, e.getMessage());
            // In a real app, we might throw an exception to trigger a retry
        }
    }
}