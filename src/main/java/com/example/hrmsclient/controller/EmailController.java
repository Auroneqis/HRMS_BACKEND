package com.example.hrmsclient.controller;

import com.example.hrmsclient.dto.ApiResponse;
import com.example.hrmsclient.dto.PageResponseDTO;
import com.example.hrmsclient.entity.EmailLog;
import com.example.hrmsclient.service.EmailLogService;
import com.example.hrmsclient.service.EmailService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emails")

public class EmailController {

    private final EmailService emailService;
    private final EmailLogService emailLogService;
    
    public EmailController(EmailService emailService, EmailLogService emailLogService) {
    	 this.emailService=emailService;
    	 this.emailLogService=emailLogService;
    }
    @PostMapping("/broadcast")
    public ResponseEntity<ApiResponse<Void>> broadcast(
            @RequestParam String[] recipients,
            @RequestParam String subject,
            @RequestBody String htmlContent) {
        emailService.sendBulkEmail(recipients, subject, htmlContent);
        return ResponseEntity.ok(ApiResponse.success(null, "Broadcast queued"));
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<PageResponseDTO<EmailLog>>> getLogs(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<EmailLog> logs = emailLogService.getLogs(page, size);
        return ResponseEntity.ok(ApiResponse.success(PageResponseDTO.from(logs), "Success"));
    }

    @GetMapping("/logs/failed")
    public ResponseEntity<ApiResponse<PageResponseDTO<EmailLog>>> getFailedLogs(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<EmailLog> logs = emailLogService.getFailedLogs(page, size);
        return ResponseEntity.ok(ApiResponse.success(PageResponseDTO.from(logs), "Success"));
    }

    @GetMapping("/logs/search")
    public ResponseEntity<ApiResponse<PageResponseDTO<EmailLog>>> searchLogs(
            @RequestParam String email,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<EmailLog> logs = emailLogService.getLogsByRecipient(email, page, size);
        return ResponseEntity.ok(ApiResponse.success(PageResponseDTO.from(logs), "Success"));
    }
}