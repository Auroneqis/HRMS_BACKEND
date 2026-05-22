package com.example.hrmsclient.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "whatsapp_logs",
    indexes = {
        @Index(name = "idx_wa_log_phone",    columnList = "toPhone"),
        @Index(name = "idx_wa_log_status",   columnList = "status"),
        @Index(name = "idx_wa_log_type",     columnList = "messageType"),
        @Index(name = "idx_wa_log_sent_at",  columnList = "sentAt")
    }
)
public class WhatsAppLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Recipient phone number in E.164 format e.g. +919876543210
    @Column(nullable = false, length = 20)
    private String toPhone;

    // Template name sent to Meta (e.g. "payslip_ready", "leave_approved")
    @Column(nullable = false, length = 100)
    private String templateName;

    // Category for filtering / analytics
    @Column(nullable = false, length = 50)
    private String messageType;

    // SENT | FAILED | PENDING
    @Column(nullable = false, length = 20)
    private String status;

    // WhatsApp message ID returned by Meta API on success
    @Column(length = 100)
    private String whatsappMessageId;

    @Column(length = 1000)
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    // ── Constructors ──────────────────────────────────────────────────────────

    public WhatsAppLog() {}

    public WhatsAppLog(String toPhone, String templateName, String messageType,
                       String status, String whatsappMessageId,
                       String errorMessage, LocalDateTime sentAt) {
        this.toPhone            = toPhone;
        this.templateName       = templateName;
        this.messageType        = messageType;
        this.status             = status;
        this.whatsappMessageId  = whatsappMessageId;
        this.errorMessage       = errorMessage;
        this.sentAt             = sentAt;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId()                     { return id; }
    public void setId(Long id)              { this.id = id; }

    public String getToPhone()              { return toPhone; }
    public void setToPhone(String v)        { this.toPhone = v; }

    public String getTemplateName()         { return templateName; }
    public void setTemplateName(String v)   { this.templateName = v; }

    public String getMessageType()          { return messageType; }
    public void setMessageType(String v)    { this.messageType = v; }

    public String getStatus()               { return status; }
    public void setStatus(String v)         { this.status = v; }

    public String getWhatsappMessageId()            { return whatsappMessageId; }
    public void setWhatsappMessageId(String v)      { this.whatsappMessageId = v; }

    public String getErrorMessage()         { return errorMessage; }
    public void setErrorMessage(String v)   { this.errorMessage = v; }

    public LocalDateTime getSentAt()        { return sentAt; }
    public void setSentAt(LocalDateTime v)  { this.sentAt = v; }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String toPhone, templateName, messageType, status, whatsappMessageId, errorMessage;
        private LocalDateTime sentAt;

        public Builder toPhone(String v)            { this.toPhone = v;            return this; }
        public Builder templateName(String v)       { this.templateName = v;       return this; }
        public Builder messageType(String v)        { this.messageType = v;        return this; }
        public Builder status(String v)             { this.status = v;             return this; }
        public Builder whatsappMessageId(String v)  { this.whatsappMessageId = v;  return this; }
        public Builder errorMessage(String v)       { this.errorMessage = v;       return this; }
        public Builder sentAt(LocalDateTime v)      { this.sentAt = v;             return this; }

        public WhatsAppLog build() {
            return new WhatsAppLog(toPhone, templateName, messageType,
                                   status, whatsappMessageId, errorMessage, sentAt);
        }
    }
}
