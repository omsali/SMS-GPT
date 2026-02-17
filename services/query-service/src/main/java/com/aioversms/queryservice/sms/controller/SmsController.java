package com.aioversms.queryservice.sms.controller;

import com.aioversms.queryservice.sms.dto.SmsRequestDto;
import com.aioversms.queryservice.sms.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/sms")
@RequiredArgsConstructor
@Slf4j
public class SmsController {

    private final SmsService smsService;

    // We removed @RequestBody and added explicit support for FORM_URLENCODED
    @PostMapping(value = "/receive", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public void receiveSms(@RequestParam Map<String, String> formData) {
        
        // Twilio sends keys as "From" and "Body" (Capitalized)
        String from = formData.get("From");
        String body = formData.get("Body");
        
        log.info("📞 Twilio Webhook Hit! From: {} | Body: {}", from, body);

        if (from == null || body == null) {
            log.warn("⚠️ Received invalid Twilio payload: {}", formData);
            return;
        }

        // Manually build the DTO since we aren't using JSON auto-mapping anymore
        SmsRequestDto request = new SmsRequestDto();
        request.setFrom(from);
        request.setText(body);

        smsService.processIncomingSms(request);
    }
}