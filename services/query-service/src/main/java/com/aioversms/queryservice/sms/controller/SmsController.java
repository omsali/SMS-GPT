package com.aioversms.queryservice.sms.controller;

import com.aioversms.queryservice.sms.dto.SmsRequestDto;
import com.aioversms.queryservice.sms.service.SmsService;
// import com.aioversms.queryservice.sms.service.SmsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    // Webhook for receiving SMS (Simulated)
    @PostMapping("/receive")
    public ResponseEntity<String> receiveSms(@RequestBody @Valid SmsRequestDto request) {
        smsService.processIncomingSms(request);
        return ResponseEntity.accepted().body("SMS Received");
    }
}