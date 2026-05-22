package com.example.hrmsclient.controller;

import com.example.hrmsclient.entity.Employee;
import com.example.hrmsclient.entity.WhatsAppLog;
import com.example.hrmsclient.repository.EmployeeRepository;
import com.example.hrmsclient.repository.WhatsAppLogRepository;
import com.example.hrmsclient.service.WhatsAppService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * WhatsApp notification management endpoints.
 *
 * BASE PATH: /api/whatsapp
 *
 * All endpoints require ADMIN or HR role.
 */
@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppController {

    private final WhatsAppService        whatsAppService;
    private final WhatsAppLogRepository  whatsAppLogRepository;
    private final EmployeeRepository     employeeRepository;

    public WhatsAppController(WhatsAppService whatsAppService,
                              WhatsAppLogRepository whatsAppLogRepository,
                              EmployeeRepository employeeRepository) {
        this.whatsAppService       = whatsAppService;
        this.whatsAppLogRepository = whatsAppLogRepository;
        this.employeeRepository    = employeeRepository;
    }

    // ── 1. Send custom message to one employee ────────────────────────────────

    /**
     * POST /api/whatsapp/send
     *
     * Body: {
     *   "employeeId": 5,
     *   "templateName": "payslip_ready",
     *   "type": "PAYSLIP",
     *   "parameters": ["Raju", "May 2026", "45000"]
     * }
     */
    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> sendManual(@RequestBody Map<String, Object> body) {
        Long employeeId   = Long.valueOf(body.get("employeeId").toString());
        String template   = (String) body.get("templateName");
        String type       = body.getOrDefault("type", "MANUAL").toString();
        List<String> params = (List<String>) body.getOrDefault("parameters", List.of());

        Employee employee = employeeRepository.findByIdAndDeletedFalse(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

        String phone = employee.getContactNumber1();
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error", "message", "Employee has no phone number on file"));
        }

        whatsAppService.sendTemplateMessage(phone, template, type, params);

        return ResponseEntity.ok(Map.of(
            "status",  "queued",
            "message", "WhatsApp message queued for " + employee.getFirstName(),
            "phone",   phone
        ));
    }

    // ── 2. Bulk attendance alert ──────────────────────────────────────────────

    /**
     * POST /api/whatsapp/attendance-alert
     * Body: { "employeeIds": [1, 2, 3], "date": "2026-05-20" }
     *
     * Sends attendance missing-punch alert to multiple employees at once.
     */
    @PostMapping("/attendance-alert")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> sendBulkAttendanceAlert(@RequestBody Map<String, Object> body) {
        List<Integer> ids = (List<Integer>) body.get("employeeIds");
        String date       = (String) body.get("date");

        int sent = 0;
        int skipped = 0;
        for (Integer id : ids) {
            Employee emp = employeeRepository.findByIdAndDeletedFalse(id.longValue()).orElse(null);
            if (emp == null || emp.getContactNumber1() == null) { skipped++; continue; }
            whatsAppService.sendAttendanceAlert(emp, date);
            sent++;
        }

        return ResponseEntity.ok(Map.of(
            "status",  "queued",
            "sent",    sent,
            "skipped", skipped,
            "date",    date
        ));
    }

    // ── 3. View logs (paginated) ──────────────────────────────────────────────

    /**
     * GET /api/whatsapp/logs?page=0&size=20
     */
    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> getLogs(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<WhatsAppLog> logs = whatsAppLogRepository.findAllByOrderBySentAtDesc(
            PageRequest.of(page, size, Sort.by("sentAt").descending())
        );

        return ResponseEntity.ok(Map.of(
            "status",      "success",
            "totalElements", logs.getTotalElements(),
            "totalPages",  logs.getTotalPages(),
            "data",        logs.getContent()
        ));
    }

    /**
     * GET /api/whatsapp/logs?status=FAILED&page=0&size=20
     */
    @GetMapping("/logs/by-status")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> getLogsByStatus(
            @RequestParam String status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<WhatsAppLog> logs = whatsAppLogRepository.findByStatusOrderBySentAtDesc(
            status.toUpperCase(),
            PageRequest.of(page, size)
        );

        return ResponseEntity.ok(Map.of(
            "status",        "success",
            "totalElements", logs.getTotalElements(),
            "data",          logs.getContent()
        ));
    }

    // ── 4. Health / stats ─────────────────────────────────────────────────────

    /**
     * GET /api/whatsapp/stats
     * Returns failure count in last 24 hours — useful for admin dashboard.
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> getStats() {
        long failures24h = whatsAppLogRepository.countFailuresSince(LocalDateTime.now().minusHours(24));
        long totalSent   = whatsAppLogRepository.count();

        return ResponseEntity.ok(Map.of(
            "status",        "success",
            "failures24h",   failures24h,
            "totalMessages", totalSent
        ));
    }
}
