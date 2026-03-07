package com.example.hrmsclient.controller;

import com.example.hrmsclient.entity.Payroll;
import com.example.hrmsclient.entity.PayrollStatus;
import com.example.hrmsclient.repository.PayrollRepository;
import com.example.hrmsclient.service.PayrollService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    private final PayrollService    payrollService;
    private final PayrollRepository payrollRepository;

    public PayrollController(PayrollService payrollService,
                             PayrollRepository payrollRepository) {
        this.payrollService    = payrollService;
        this.payrollRepository = payrollRepository;
    }

    // ── GENERATE — Admin/HR manually triggers payroll generation ─────────────
    // POST /api/payroll/generate?month=2024-03-01
    @PostMapping("/generate")
    public ResponseEntity<?> generatePayroll(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month,
            @AuthenticationPrincipal UserDetails userDetails) {

        payrollService.generatePayrollForAllEmployees(month);
        return ResponseEntity.ok(Map.of(
            "status",  "success",
            "message", "Payroll generated for " + month.getMonth() + " " + month.getYear()
        ));
    }
    // PUT /api/payroll/{id}/approve
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approvePayroll(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Payroll payroll = payrollService.approvePayroll(id, userDetails.getUsername());
        return ResponseEntity.ok(Map.of(
            "status",  "success",
            "message", "Payroll approved successfully",
            "data",    buildPayrollSummary(payroll)
        ));
    }
    // POST /api/payroll/{id}/process-payment
    @PostMapping("/{id}/process-payment")
    public ResponseEntity<?> processPayment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Payroll payroll = payrollService.manualProcessPayment(id, userDetails.getUsername());
        return ResponseEntity.ok(Map.of(
            "status",  "success",
            "message", "✅ Salary of ₹" + payroll.getNetSalary() + " transferred successfully",
            "data",    buildPayrollSummary(payroll)
        ));
    }
    // GET /api/payroll/month?month=2024-03-01
    @GetMapping("/month")
    public ResponseEntity<?> getPayrollByMonth(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("employee.firstName"));
        Page<Payroll> payrolls  = payrollRepository.findByPayrollMonth(month, pageRequest);

        return ResponseEntity.ok(Map.of(
            "status", "success",
            "data",   payrolls.getContent().stream().map(this::buildPayrollSummary).toList(),
            "totalRecords", payrolls.getTotalElements(),
            "totalPages",   payrolls.getTotalPages()
        ));
    }
    // GET /api/payroll/employee/{employeeId}
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> getEmployeePayrollHistory(
            @PathVariable Long employeeId) {

        List<Payroll> payrolls = payrollRepository.findByEmployeeId(employeeId);
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "data",   payrolls.stream().map(this::buildPayrollSummary).toList()
        ));
    }
    // GET /api/payroll/my-payslips
    @GetMapping("/my-payslips")
    public ResponseEntity<?> getMyPayslips(
            @AuthenticationPrincipal UserDetails userDetails) {

        // Employee can only see own payslips — identified by JWT email
        return ResponseEntity.ok(Map.of(
            "status",  "success",
            "message", "Use /api/payroll/employee/{your-employee-id}"
        ));
    }
    // GET /api/payroll/summary?month=2024-03-01
    @GetMapping("/summary")
    public ResponseEntity<?> getPayrollSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {

        return ResponseEntity.ok(Map.of(
            "status", "success",
            "data", Map.of(
                "month",       month.getMonth() + " " + month.getYear(),
                "totalPaid",   payrollRepository.countByMonthAndStatus(month, PayrollStatus.PAID),
                "totalPending",payrollRepository.countByMonthAndStatus(month, PayrollStatus.PENDING),
                "totalAmount", payrollRepository.getTotalNetSalaryByMonth(month) != null
                               ? payrollRepository.getTotalNetSalaryByMonth(month) : 0
            )
        ));
    }
    private Map<String, Object> buildPayrollSummary(Payroll p) {
        return Map.ofEntries(
            Map.entry("payrollId",       p.getId()),
            Map.entry("employeeId",      p.getEmployee().getEmployeeId()),
            Map.entry("employeeName",    p.getEmployee().getFullName()),
            Map.entry("month",           p.getPayrollMonth().toString()),
            Map.entry("basicSalary",     p.getBasicSalary()),
            Map.entry("grossSalary",     p.getGrossSalary()),
            Map.entry("totalDeductions", p.getTotalDeductions()),
            Map.entry("netSalary",       p.getNetSalary()),
            Map.entry("status",          p.getStatus().toString()),
            Map.entry("paymentDate",     p.getPaymentDate() != null
                                            ? p.getPaymentDate().toString()
                                            : "Not Paid"),
            Map.entry("paymentRef",      p.getPaymentReference() != null
                                            ? p.getPaymentReference()
                                            : "N/A")
        );
    }
}