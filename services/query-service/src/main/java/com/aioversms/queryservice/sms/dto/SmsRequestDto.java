package com.aioversms.queryservice.sms.dto;
// package com.aioversms.queryservice.sms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SmsRequestDto {
    @NotBlank(message = "Phone number is required")
    private String from; // Sender's phone number

    @NotBlank(message = "Message body is required")
    private String text; // The actual query (e.g., "Best fertilizer for corn?")
}