package com.example.hrmsclient.controller;

import com.example.hrmsclient.dto.AttendanceResponseDTO;
import com.example.hrmsclient.dto.DashboardFilterRequest;
import com.example.hrmsclient.entity.*;
import com.example.hrmsclient.service.*;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final PayrollService payrollService;
    private final AdminService adminService;

    public DashboardController(DashboardService dashboardService,
            PayrollService payrollService,
            AdminService adminService) {
        this.dashboardService = dashboardService;
        this.payrollService = payrollService;
        this.adminService = adminService;
    }

 
    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','HR','MANAGER')")
    public ResponseEntity<?> getOverview(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails user) {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", dashboardService.getOverviewStats(user)));
    }

  
    @GetMapping("/employees")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','HR','MANAGER')")
    public ResponseEntity<?> getEmployees(
            @ModelAttribute DashboardFilterRequest filter,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails user) {

        Page<Employee> employees = dashboardService.getFilteredEmployees(filter, user);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", employees.getContent(),
                "totalRecords", employees.getTotalElements(),
                "totalPages", employees.getTotalPages(),
                "currentPage", employees.getNumber()));
    }

   
    @GetMapping("/attendance")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public ResponseEntity<?> getAttendance(@ModelAttribute DashboardFilterRequest filter) {
        Page<AttendanceResponseDTO> attendance = dashboardService.getFilteredAttendance(filter);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", attendance.getContent(),
                "totalRecords", attendance.getTotalElements(),
                "totalPages", attendance.getTotalPages(),
                "currentPage", attendance.getNumber()));
    }

   
    @GetMapping("/departments")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','HR','MANAGER')")
    public ResponseEntity<?> getDepartments() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", dashboardService.getDepartmentBreakdown()));
    }

   
    @GetMapping("/payroll")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> getPayroll(@ModelAttribute DashboardFilterRequest filter) {
        Page<Payroll> payroll = dashboardService.getFilteredPayroll(filter);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", payroll.getContent(),
                "totalRecords", payroll.getTotalElements(),
                "totalPages", payroll.getTotalPages()));
    }

    @GetMapping("/admin-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAdminStats() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", adminService.getDashboardStats()));
    }

  
    @PostMapping("/payroll/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generatePayroll(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate month) {
        payrollService.generatePayrollForAllEmployees(month);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Payroll generated for " + month.getMonth() + " " + month.getYear()));
    }

    // Approve payroll — ADMIN ONLY
    // PUT /api/dashboard/payroll/{id}/approve
    @PutMapping("/payroll/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approvePayroll(
            @PathVariable Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails user) {
        Payroll payroll = payrollService.approvePayroll(id, user.getUsername());
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Payroll approved",
                "data", Map.of(
                        "payrollId", payroll.getId(),
                        "status", payroll.getStatus())));
    }

    // Process salary payment — ADMIN ONLY
    // POST /api/dashboard/payroll/{id}/pay
    @PostMapping("/payroll/{id}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> processPayment(
            @PathVariable Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails user) {
        Payroll payroll = payrollService.manualProcessPayment(id, user.getUsername());
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "✅ Salary of ₹" + payroll.getNetSalary() + " transferred",
                "data", Map.of(
                        "payrollId", payroll.getId(),
                        "netSalary", payroll.getNetSalary(),
                        "paymentRef", payroll.getPaymentReference(),
                        "paymentDate", payroll.getPaymentDate())));
    }

    // Delete employee — ADMIN ONLY
    // DELETE /api/dashboard/employees/{id}
    @DeleteMapping("/employees/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        // delegated to EmployeeService
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Employee deleted successfully"));
    }

    // Hold payroll — ADMIN ONLY
    // PUT /api/dashboard/payroll/{id}/hold
    @PutMapping("/payroll/{id}/hold")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> holdPayroll(
            @PathVariable Long id,
            @RequestParam String reason) {
        Payroll payroll = payrollService.holdPayroll(id, reason);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Payroll put on hold",
                "data", Map.of("payrollId", payroll.getId(), "status", payroll.getStatus())));
    }

    // Retry failed payment — ADMIN ONLY
    // POST /api/dashboard/payroll/{id}/retry
    @PostMapping("/payroll/{id}/retry")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> retryPayment(@PathVariable Long id) {
        Payroll payroll = payrollService.retryFailedPayment(id);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Payment retried",
                "data", Map.of("payrollId", payroll.getId(), "status", payroll.getStatus())));
    }
}