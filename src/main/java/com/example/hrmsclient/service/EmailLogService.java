package com.example.hrmsclient.service;

import com.example.hrmsclient.entity.EmailLog;
import com.example.hrmsclient.repository.EmailLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailLogService {

    private final EmailLogRepository emailLogRepo;

    // ✅ Manual constructor
    public EmailLogService(EmailLogRepository emailLogRepo) {
        this.emailLogRepo = emailLogRepo;
    }

    // ✅ Log successful email
    public void logSuccess(String to, String subject, String type) {
        emailLogRepo.save(
            EmailLog.builder()
                .recipientEmail(to)
                .subject(subject)
                .emailType(type)
                .status("SENT")
                .sentAt(LocalDateTime.now())
                .build()
        );
    }

    // ✅ Log failed email
    public void logFailure(String to, String subject, String type, String errorMsg) {
        emailLogRepo.save(
            EmailLog.builder()
                .recipientEmail(to)
                .subject(subject)
                .emailType(type)
                .status("FAILED")
                .errorMessage(errorMsg)
                .sentAt(LocalDateTime.now())
                .build()
        );
    }

    // ✅ Get all logs paginated
    public Page<EmailLog> getLogs(int page, int size) {
        return emailLogRepo.findAll(
            PageRequest.of(page, size, Sort.by("sentAt").descending())
        );
    }

    // ✅ Search logs by recipient email
    public Page<EmailLog> getLogsByRecipient(String email, int page, int size) {
        return emailLogRepo.findByRecipientEmailContainingIgnoreCase(
            email,
            PageRequest.of(page, size, Sort.by("sentAt").descending())
        );
    }

    // ✅ Get only failed logs
    public Page<EmailLog> getFailedLogs(int page, int size) {
        return emailLogRepo.findByStatus(
            "FAILED",
            PageRequest.of(page, size, Sort.by("sentAt").descending())
        );
    }

    //  Count failed emails
    public long countFailed() {
        return emailLogRepo.countByStatus("FAILED");
    }

    //  Count sent emails
    public long countSent() {
        return emailLogRepo.countByStatus("SENT");
    }
}