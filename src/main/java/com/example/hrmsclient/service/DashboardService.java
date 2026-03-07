package com.example.hrmsclient.service;

import com.example.hrmsclient.dto.DashboardFilterRequest;
import com.example.hrmsclient.entity.*;
import com.example.hrmsclient.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;                       

import java.time.LocalDate;
import java.util.*;

@Service
public class DashboardService {

    private final EmployeeRepository   employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayrollRepository    payrollRepository;

    public DashboardService(EmployeeRepository employeeRepository,
                             AttendanceRepository attendanceRepository,
                             PayrollRepository payrollRepository) {
        this.employeeRepository   = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.payrollRepository    = payrollRepository;
    }

    // ── 1. Overview Stats (ADMIN + HR + MANAGER) ──────────────────────────────
    public Map<String, Object> getOverviewStats() {
        LocalDate today      = LocalDate.now();
        long totalEmployees  = employeeRepository.countByDeletedFalse();
        long activeEmployees = employeeRepository
                .countByEmploymentStatusAndDeletedFalse(EmploymentStatus.ACTIVE);
        long presentToday    = attendanceRepository
                .countByAttendanceDateAndStatus(today, AttendanceStatus.PRESENT);
        long onLeaveToday    = attendanceRepository
                .countByAttendanceDateAndStatus(today, AttendanceStatus.ON_LEAVE);
        long absentToday     = Math.max(activeEmployees - presentToday - onLeaveToday, 0);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalEmployees",  totalEmployees);
        stats.put("activeEmployees", activeEmployees);
        stats.put("presentToday",    presentToday);
        stats.put("onLeaveToday",    onLeaveToday);
        stats.put("absentToday",     absentToday);
        stats.put("attendanceRate",  activeEmployees > 0
                ? Math.round((presentToday * 100.0) / activeEmployees) + "%" : "0%");
        return stats;
    }

    // ── 2. Employees with filters ─────────────────────────────────────────────
    public Page<Employee> getFilteredEmployees(DashboardFilterRequest f) {
        Pageable pg = pageable(f);

        if (hasValue(f.getSearch()))           return employeeRepository.searchEmployees(f.getSearch(), pg);
        if (hasValue(f.getEmployeeId()))       return employeeRepository.searchEmployees(f.getEmployeeId(), pg);
        if (hasValue(f.getFirstName()))        return employeeRepository.searchEmployees(f.getFirstName(), pg);
        if (hasValue(f.getDepartment()))       return employeeRepository.findByDepartmentIgnoreCaseAndDeletedFalse(f.getDepartment(), pg);
        if (hasValue(f.getRole()))             return employeeRepository.findByRoleIgnoreCaseAndDeletedFalse(f.getRole(), pg);
        if (hasValue(f.getEmploymentStatus())) {
            EmploymentStatus s = EmploymentStatus.valueOf(f.getEmploymentStatus().toUpperCase());
            return employeeRepository.findByEmploymentStatusAndDeletedFalse(s, pg);
        }
        return employeeRepository.findByDeletedFalse(pg);
    }

   

 // ── 3. Attendance with filters ────────────────────────────────────────────
 @Transactional(readOnly = true)
 public Page<com.example.hrmsclient.dto.AttendanceResponseDTO> getFilteredAttendance(DashboardFilterRequest f) {

     Pageable  pg   = pageable(f);
     LocalDate date = f.getAttendanceDate() != null ? f.getAttendanceDate() : LocalDate.now();

     Page<Attendance> attendancePage;

     if (f.getAttendanceDateFrom() != null && f.getAttendanceDateTo() != null) {
         attendancePage = attendanceRepository.findByAttendanceDateBetween(
                 f.getAttendanceDateFrom(), f.getAttendanceDateTo(), pg);
     }
     else if (hasValue(f.getAttendanceStatus())) {
         AttendanceStatus s = AttendanceStatus.valueOf(f.getAttendanceStatus().toUpperCase());
         attendancePage = attendanceRepository.findByStatusAndAttendanceDate(s, date, pg);
     }
     else {
         attendancePage = attendanceRepository.findByAttendanceDate(date, pg);
     }

     return attendancePage.map(com.example.hrmsclient.dto.AttendanceResponseDTO::from);
 }

    // ── 4. Payroll with filters ───────────────────────────────────────────────
    public Page<Payroll> getFilteredPayroll(DashboardFilterRequest f) {
        Pageable  pg    = pageable(f);
        LocalDate month = f.getPayrollMonth() != null
                ? f.getPayrollMonth().withDayOfMonth(1)
                : LocalDate.now().withDayOfMonth(1);

        if (hasValue(f.getPayrollStatus())) {
            PayrollStatus s = PayrollStatus.valueOf(f.getPayrollStatus().toUpperCase());
            return payrollRepository.findByPayrollMonthAndStatus(month, s, pg);
        }
        return payrollRepository.findByPayrollMonth(month, pg);
    }

    // ── 5. Department breakdown ───────────────────────────────────────────────
    public List<Map<String, Object>> getDepartmentBreakdown() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : employeeRepository.countByDepartment()) {
            result.add(Map.of(
                "department", row[0] != null ? row[0] : "Unassigned",
                "count",      row[1]
            ));
        }
        return result;
    }

    private Pageable pageable(DashboardFilterRequest f) {
        Sort sort = f.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(f.getSortBy()).ascending()
                : Sort.by(f.getSortBy()).descending();
        return PageRequest.of(f.getPage(), f.getSize(), sort);
    }

    private boolean hasValue(String s) { return s != null && !s.isBlank(); }
}