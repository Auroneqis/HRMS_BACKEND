package com.example.hrmsclient.service;

import com.example.hrmsclient.dto.AttendanceResponseDTO;
import com.example.hrmsclient.dto.DashboardFilterRequest;
import com.example.hrmsclient.entity.*;
import com.example.hrmsclient.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * UPDATED DashboardService
 *
 * Changes vs original:
 * 1. getOverviewStats: now includes presentToday (was missing/not working).
 * Uses attendanceRepository.countByAttendanceDateAndStatus for today.
 *
 * 2. Removed pendingLeaves from admin overview stats (per requirements:
 * "Need to remove Pending Leaves from admin dashboard").
 *
 * 3. Added managerPendingLeaveCount in getOverviewStats —
 * "Manager Pending Leave Approval" tile on dashboard.
 */
@Service
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayrollRepository payrollRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    public DashboardService(EmployeeRepository employeeRepository,
            AttendanceRepository attendanceRepository,
            PayrollRepository payrollRepository,
            LeaveRequestRepository leaveRequestRepository) {
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.payrollRepository = payrollRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public Map<String, Object> getOverviewStats(org.springframework.security.core.userdetails.UserDetails user) {
        LocalDate today = LocalDate.now();

        Optional<Long> managerId = resolveManagerId(user);

        long totalEmployees;
        long activeEmployees;
        long presentToday;
        long leaveToday;

        if (managerId.isPresent()) {
            // MANAGER: show stats for assigned employees only
            Long mgrId = managerId.get();
            totalEmployees = employeeRepository.countByManagerIdAndDeletedFalse(mgrId);
            activeEmployees = employeeRepository
                    .countByManagerIdAndEmploymentStatusAndDeletedFalse(mgrId, EmploymentStatus.ACTIVE);
        } else {
            // ADMIN/HR/SUPER_ADMIN: show global stats
            totalEmployees = employeeRepository.countByDeletedFalse();
            activeEmployees = employeeRepository
                    .countByEmploymentStatusAndDeletedFalse(EmploymentStatus.ACTIVE);
        }

        // FIX: presentToday — count PRESENT + WFH records for today
        presentToday = attendanceRepository
                .countByAttendanceDateAndStatus(today, AttendanceStatus.PRESENT)
                + attendanceRepository
                        .countByAttendanceDateAndStatus(today, AttendanceStatus.WORK_FROM_HOME);

        // leaveToday — employees on approved leave today
        leaveToday = attendanceRepository
                .countByAttendanceDateAndStatus(today, AttendanceStatus.ON_LEAVE);

        // notPresentToday = active - present - onLeave
        long notPresentToday = Math.max(activeEmployees - presentToday - leaveToday, 0);

        // managerPendingLeaveCount — total pending leaves where employee has a
        // reporting manager
        long managerPendingLeaveCount = leaveRequestRepository
                .countPendingLeavesWithReportingManager();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalEmployees", totalEmployees);
        stats.put("activeEmployees", activeEmployees);
        stats.put("presentToday", presentToday); // FIX: was missing
        stats.put("notPresentToday", notPresentToday);
        stats.put("leaveToday", leaveToday);
        // pendingLeaves REMOVED from admin dashboard per requirements
        stats.put("managerPendingLeaveCount", managerPendingLeaveCount); // NEW
        return stats;
    }

    public Map<String, Object> getOverviewStats() {
        return getOverviewStats(null);
    }

    // ── Employees with Filters
    public Page<Employee> getFilteredEmployees(DashboardFilterRequest filter,
            org.springframework.security.core.userdetails.UserDetails user) {
        Pageable pg = pageable(filter);

        Optional<Long> managerId = resolveManagerId(user);
        if (managerId.isPresent()) {
            Long mgrId = managerId.get();
            if (hasValue(filter.getSearch()))
                return employeeRepository.searchEmployeesByManager(mgrId, filter.getSearch(), pg);
            if (hasValue(filter.getEmployeeId()))
                return employeeRepository.searchEmployeesByManager(mgrId, filter.getEmployeeId(), pg);
            if (hasValue(filter.getFirstName()))
                return employeeRepository.searchEmployeesByManager(mgrId, filter.getFirstName(), pg);
            if (hasValue(filter.getDepartment()))
                return employeeRepository.findByManagerIdAndDepartmentIgnoreCaseAndDeletedFalse(mgrId,
                        filter.getDepartment(), pg);
            if (hasValue(filter.getRole()))
                return employeeRepository.findByManagerIdAndRoleIgnoreCaseAndDeletedFalse(mgrId, filter.getRole(), pg);
            if (hasValue(filter.getEmploymentStatus())) {
                EmploymentStatus s = EmploymentStatus.valueOf(filter.getEmploymentStatus().toUpperCase());
                return employeeRepository.findByManagerIdAndEmploymentStatusAndDeletedFalse(mgrId, s, pg);
            }
            return employeeRepository.findByManagerIdAndDeletedFalse(mgrId, pg);
        }

        if (hasValue(filter.getSearch()))
            return employeeRepository.searchEmployees(filter.getSearch(), pg);
        if (hasValue(filter.getEmployeeId()))
            return employeeRepository.searchEmployees(filter.getEmployeeId(), pg);
        if (hasValue(filter.getFirstName()))
            return employeeRepository.searchEmployees(filter.getFirstName(), pg);
        if (hasValue(filter.getDepartment()))
            return employeeRepository.findByDepartmentIgnoreCaseAndDeletedFalse(filter.getDepartment(), pg);
        if (hasValue(filter.getRole()))
            return employeeRepository.findByRoleIgnoreCaseAndDeletedFalse(filter.getRole(), pg);
        if (hasValue(filter.getEmploymentStatus())) {
            EmploymentStatus s = EmploymentStatus.valueOf(filter.getEmploymentStatus().toUpperCase());
            return employeeRepository.findByEmploymentStatusAndDeletedFalse(s, pg);
        }
        return employeeRepository.findAllByDeletedFalse(pg);
    }

    public Page<Employee> getFilteredEmployees(DashboardFilterRequest filter) {
        return getFilteredEmployees(filter, null);
    }

    private Optional<Long> resolveManagerId(org.springframework.security.core.userdetails.UserDetails user) {
        if (user == null) {
            return Optional.empty();
        }

        boolean isManager = user.getAuthorities().stream()
                .anyMatch(ga -> "ROLE_MANAGER".equalsIgnoreCase(ga.getAuthority()));

        if (!isManager) {
            return Optional.empty();
        }

        String username = user.getUsername();
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        Optional<Employee> byEmail = employeeRepository.findByEmailIdAndDeletedFalse(username);
        if (byEmail.isPresent()) {
            return byEmail.map(Employee::getId);
        }

        Optional<Employee> byEmployeeId = employeeRepository.findByEmployeeIdAndDeletedFalse(username);
        return byEmployeeId.map(Employee::getId);
    }

    // ── Attendance with Filters
    @Transactional(readOnly = true)
    public Page<AttendanceResponseDTO> getFilteredAttendance(DashboardFilterRequest f) {
        Pageable pg = pageable(f);
        LocalDate date = f.getAttendanceDate() != null ? f.getAttendanceDate() : LocalDate.now();

        Page<Attendance> attendancePage;
        if (f.getAttendanceDateFrom() != null && f.getAttendanceDateTo() != null) {
            attendancePage = attendanceRepository.findByAttendanceDateBetween(
                    f.getAttendanceDateFrom(), f.getAttendanceDateTo(), pg);
        } else {
            attendancePage = attendanceRepository.findByAttendanceDate(date, pg);
        }
        return attendancePage.map(this::toAttendanceDTO);
    }

    // ── Department Breakdown
    public Map<String, Long> getDepartmentBreakdown() {

        List<Employee> all = employeeRepository
                .findAllByDeletedFalse(Pageable.unpaged())
                .getContent();

        Map<String, Long> breakdown = new LinkedHashMap<>();

        for (Employee e : all) {
            breakdown.merge(e.getDepartment(), 1L, Long::sum);
        }

        return breakdown;
    }

    // ── Payroll with Filters
    public Page<Payroll> getFilteredPayroll(DashboardFilterRequest f) {
        Pageable pg = pageable(f);
        if (f.getPayrollMonth() != null && hasValue(f.getPayrollStatus())) {
            PayrollStatus s = PayrollStatus.valueOf(f.getPayrollStatus().toUpperCase());
            return payrollRepository.findByPayrollMonthAndStatus(f.getPayrollMonth(), s, pg);
        }
        if (f.getPayrollMonth() != null) {
            return payrollRepository.findByPayrollMonth(f.getPayrollMonth(), pg);
        }
        return payrollRepository.findAll(pg);
    }

    // ── Helpers
    private AttendanceResponseDTO toAttendanceDTO(Attendance a) {
        return AttendanceResponseDTO.from(a);
    }

    private Pageable pageable(DashboardFilterRequest f) {
        Sort sort = Sort.by(
                hasValue(f.getSortDir()) && f.getSortDir().equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                hasValue(f.getSortBy()) ? f.getSortBy() : "createdAt");
        return PageRequest.of(f.getPage(), f.getSize(), sort);
    }

    private boolean hasValue(String s) {
        return s != null && !s.isBlank();
    }
}