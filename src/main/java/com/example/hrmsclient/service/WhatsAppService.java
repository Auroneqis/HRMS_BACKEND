package com.example.hrmsclient.service;

import com.example.hrmsclient.entity.Employee;
import com.example.hrmsclient.entity.Payroll;
import com.example.hrmsclient.entity.WhatsAppLog;
import com.example.hrmsclient.repository.WhatsAppLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * WhatsApp notification service using Meta WhatsApp Business Cloud API.
 *
 * HOW IT WORKS:
 *  1. You create message templates in Meta Business Manager (free).
 *  2. This service calls the Meta API with the template name + parameters.
 *  3. Meta delivers the WhatsApp message to the employee's phone.
 *
 * SETUP (one-time):
 *  1. Create a Meta Developer account at developers.facebook.com
 *  2. Create a WhatsApp Business App → get Phone Number ID + Access Token
 *  3. Add the 5 properties below to application-dev.properties
 *  4. Create templates in Meta Business Manager (examples provided below)
 *
 * REQUIRED PROPERTIES in application-dev.properties:
 *   whatsapp.api.token=your_permanent_access_token
 *   whatsapp.api.phone-number-id=your_phone_number_id
 *   whatsapp.api.base-url=https://graph.facebook.com/v19.0
 *   app.company.name=MSR Informatic Solutions Pvt Ltd
 *   whatsapp.enabled=true
 */
@Service
public class WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);

    private final RestTemplate            restTemplate;
    private final WhatsAppLogRepository   whatsAppLogRepository;

    @Value("${whatsapp.api.token:}")
    private String apiToken;

    @Value("${whatsapp.api.phone-number-id:}")
    private String phoneNumberId;

    @Value("${whatsapp.api.base-url:https://graph.facebook.com/v19.0}")
    private String baseUrl;

    @Value("${app.company.name:HRMS}")
    private String companyName;

    // Set to false in dev/test to skip actual API calls but still log
    @Value("${whatsapp.enabled:false}")
    private boolean enabled;

    public WhatsAppService(WhatsAppLogRepository whatsAppLogRepository) {
        this.restTemplate          = new RestTemplate();
        this.whatsAppLogRepository = whatsAppLogRepository;
    }

    // ── Public API (called from PayrollService, LeaveService, etc.) ───────────

    /**
     * Notify employee that their payslip is ready.
     * Meta template name: "payslip_ready"
     * Template body: "Hi {{1}}, your payslip for {{2}} is ready. Net pay: ₹{{3}}. Login to HRMS to download."
     */
    @Async
    public void sendPayslipNotification(Employee employee, Payroll payroll) {
        String phone = resolvePhone(employee);
        if (phone == null) return;

        String month   = payroll.getPayrollMonth().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        String netPay  = payroll.getNetSalary() != null
                         ? String.format("%.0f", payroll.getNetSalary()) : "0";

        sendTemplateMessage(
            phone,
            "payslip_ready",
            "PAYSLIP",
            List.of(employee.getFirstName(), month, netPay)
        );
    }

    /**
     * Notify employee that their leave request was approved or rejected.
     * Meta template name: "leave_status_update"
     * Template body: "Hi {{1}}, your {{2}} leave from {{3}} to {{4}} has been {{5}}."
     */
    @Async
    public void sendLeaveStatusNotification(Employee employee,
                                            String leaveType,
                                            String fromDate,
                                            String toDate,
                                            String status) {
        String phone = resolvePhone(employee);
        if (phone == null) return;

        sendTemplateMessage(
            phone,
            "leave_status_update",
            "LEAVE",
            List.of(employee.getFirstName(), leaveType, fromDate, toDate, status)
        );
    }

    /**
     * Notify employee about attendance irregularity (missing check-in/out).
     * Meta template name: "attendance_alert"
     * Template body: "Hi {{1}}, we noticed a missing punch on {{2}}. Please update your attendance."
     */
    @Async
    public void sendAttendanceAlert(Employee employee, String date) {
        String phone = resolvePhone(employee);
        if (phone == null) return;

        sendTemplateMessage(
            phone,
            "attendance_alert",
            "ATTENDANCE",
            List.of(employee.getFirstName(), date)
        );
    }

    /**
     * Notify employee of a salary advance credit.
     * Meta template name: "salary_advance"
     * Template body: "Hi {{1}}, a salary advance of ₹{{2}} has been credited to your account."
     */
    @Async
    public void sendSalaryAdvanceNotification(Employee employee, String amount) {
        String phone = resolvePhone(employee);
        if (phone == null) return;

        sendTemplateMessage(
            phone,
            "salary_advance",
            "ADVANCE",
            List.of(employee.getFirstName(), amount)
        );
    }

    /**
     * General-purpose template sender. Use this if you add new templates later.
     * @param to         Phone in E.164 format (+91XXXXXXXXXX)
     * @param template   Template name registered in Meta Business Manager
     * @param type       Category label for logs (e.g. "CUSTOM")
     * @param parameters List of {{1}}, {{2}} ... values in order
     */
    @Async
    public void sendTemplateMessage(String to,
                                    String template,
                                    String type,
                                    List<String> parameters) {
        String cleanPhone = normalizePhone(to);
        if (cleanPhone == null) {
            log.warn("WhatsApp | invalid phone number, skipping: {}", to);
            return;
        }

        if (!enabled) {
            log.info("WhatsApp | DISABLED — would send '{}' to {}", template, cleanPhone);
            saveLog(cleanPhone, template, type, "SKIPPED", null, "WhatsApp disabled in config");
            return;
        }

        String url     = baseUrl + "/" + phoneNumberId + "/messages";
        String payload = buildPayload(cleanPhone, template, parameters);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                Map.class
            );

            String messageId = extractMessageId(response.getBody());
            log.info("WhatsApp | sent '{}' to {} | msgId={}", template, cleanPhone, messageId);
            saveLog(cleanPhone, template, type, "SENT", messageId, null);

        } catch (Exception ex) {
            log.error("WhatsApp | FAILED '{}' to {} | {}", template, cleanPhone, ex.getMessage());
            saveLog(cleanPhone, template, type, "FAILED", null, ex.getMessage());
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Build the Meta API JSON payload for a template message.
     *
     * JSON structure:
     * {
     *   "messaging_product": "whatsapp",
     *   "to": "919876543210",
     *   "type": "template",
     *   "template": {
     *     "name": "payslip_ready",
     *     "language": { "code": "en" },
     *     "components": [{
     *       "type": "body",
     *       "parameters": [
     *         {"type":"text","text":"Raju"},
     *         {"type":"text","text":"May 2026"},
     *         {"type":"text","text":"45000"}
     *       ]
     *     }]
     *   }
     * }
     */
    private String buildPayload(String phone, String templateName, List<String> params) {
        StringBuilder paramsJson = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) paramsJson.append(",");
            // Escape quotes in parameter values
            String safe = params.get(i).replace("\"", "\\\"");
            paramsJson.append("{\"type\":\"text\",\"text\":\"").append(safe).append("\"}");
        }

        return "{"
            + "\"messaging_product\":\"whatsapp\","
            + "\"to\":\"" + phone + "\","
            + "\"type\":\"template\","
            + "\"template\":{"
            +   "\"name\":\"" + templateName + "\","
            +   "\"language\":{\"code\":\"en\"},"
            +   "\"components\":[{"
            +     "\"type\":\"body\","
            +     "\"parameters\":[" + paramsJson + "]"
            +   "}]"
            + "}"
            + "}";
    }

    /**
     * Resolve employee phone: prefer contactNumber1, strip non-digits, add +91.
     */
    private String resolvePhone(Employee employee) {
        String raw = employee.getContactNumber1();
        return normalizePhone(raw);
    }

    /**
     * Normalize a raw phone string to E.164 (+91XXXXXXXXXX for India).
     * Returns null if the number is blank or too short to be valid.
     */
    private String normalizePhone(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() < 10) return null;
        // Already has country code (91 + 10 digits = 12)
        if (digits.length() == 12 && digits.startsWith("91")) return "+" + digits;
        // 10-digit number — assume India
        if (digits.length() == 10) return "+91" + digits;
        // Already E.164 without +
        if (digits.length() > 10) return "+" + digits;
        return null;
    }

    @SuppressWarnings("unchecked")
    private String extractMessageId(Map<?, ?> body) {
        try {
            List<?> messages = (List<?>) body.get("messages");
            if (messages != null && !messages.isEmpty()) {
                return (String) ((Map<?, ?>) messages.get(0)).get("id");
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void saveLog(String phone, String template, String type,
                         String status, String messageId, String error) {
        try {
            WhatsAppLog log = WhatsAppLog.builder()
                .toPhone(phone)
                .templateName(template)
                .messageType(type)
                .status(status)
                .whatsappMessageId(messageId)
                .errorMessage(error)
                .sentAt(LocalDateTime.now())
                .build();
            whatsAppLogRepository.save(log);
        } catch (Exception e) {
            WhatsAppService.log.warn("WhatsApp | could not save log: {}", e.getMessage());
        }
    }
}
