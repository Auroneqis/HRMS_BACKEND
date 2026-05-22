package com.example.hrmsclient.service;

import com.example.hrmsclient.dto.*;
import com.example.hrmsclient.entity.*;
import com.example.hrmsclient.repository.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;

@Service
public class LeaveService {

    private static final Logger log = Logger.getLogger(LeaveService.class.getName());

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository     employeeRepository;
    private final HrmsEmailService       hrmsEmailService;
    private final LeavePolicyService     leavePolicyService;

    public LeaveService(LeaveRequestRepository leaveRequestRepository,
                        LeaveBalanceRepository leaveBalanceRepository,
                        EmployeeRepository employeeRepository,
                        HrmsEmailService hrmsEmailService,
                        LeavePolicyService leavePolicyService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.employeeRepository     = employeeRepository;
        this.hrmsEmailService       = hrmsEmailService;
        this.leavePolicyService     = leavePolicyService;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isManager(Employee user) {
        return user != null && "MANAGER".equalsIgnoreCase(user.getRole());
    }

    private int getCurrentFYYear() {
        LocalDate today = LocalDate.now();
        return today.getMonthValue() >= 4 ? today.getYear() : today.getYear() - 1;
    }

    private LeaveRequest getById(Long id) {
        return leaveRequestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Leave request not found: " + id));
    }

    // ── Apply Leave ───────────────────────────────────────────────────────────

    @Transactional
    public LeaveResponseDTO applyLeave(LeaveRequestDTO dto) {

        Employee employee = employeeRepository.findByIdAndDeletedFalse(dto.getEmployeeId())
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        LeaveType leaveType = LeaveType.fromString(dto.getLeaveType());

        validateLeaveEligibility(employee, leaveType, dto.getStartDate(), dto.getEndDate());

        LeaveRequest request = new LeaveRequest();
        request.setEmployee(employee);
        request.setLeaveType(leaveType.name());
        request.setStartDate(dto.getStartDate());
        request.setEndDate(dto.getEndDate());
        request.setReason(dto.getReason());
        request.setStatus(LeaveStatus.PENDING);

        LeaveRequest saved = leaveRequestRepository.save(request);
        hrmsEmailService.sendLeaveAppliedEmail(saved);
        return toDTO(saved);
    }

    // ── Approve Leave ─────────────────────────────────────────────────────────

    @Transactional
    public LeaveResponseDTO approveLeave(Long id) {
        LeaveRequest request = getById(id);

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only PENDING leave can be approved");
        }

        request.setStatus(LeaveStatus.APPROVED);
        request.setApprovedAt(java.time.LocalDateTime.now());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String approvedBy = (auth != null) ? auth.getName() : "SYSTEM";
        request.setApprovedBy(approvedBy);

        LeaveRequest saved = leaveRequestRepository.save(request);

        try {
            hrmsEmailService.sendLeaveApprovedEmailAsync(
                saved.getEmployee().getEmailId(),
                saved.getEmployee().getFullName(),
                saved.getStartDate(),
                saved.getEndDate(),
                saved.getLeaveType(),
                approvedBy
            );
        } catch (Exception ex) {
            log.severe("Email failed: " + ex.getMessage());
        }

        return toDTO(saved);
    }

    // ── Reject Leave ──────────────────────────────────────────────────────────

    @Transactional
    public LeaveResponseDTO rejectLeave(Long id, String reason) {
        LeaveRequest request = getById(id);

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only PENDING leave can be rejected");
        }

        request.setStatus(LeaveStatus.REJECTED);
        request.setRejectionReason(reason);

        LeaveRequest saved = leaveRequestRepository.save(request);
        hrmsEmailService.sendLeaveRejectedEmail(saved);

        return toDTO(saved);
    }

    // ── Leave Balance ─────────────────────────────────────────────────────────

    public Map<String, Object> getMyLeaveBalance(Long empDbId) {
        Employee emp = employeeRepository.findByIdAndDeletedFalse(empDbId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        int fyYear = getCurrentFYYear();
        LocalDate fyStart = LocalDate.of(fyYear, 4, 1);
        LocalDate fyEnd   = LocalDate.of(fyYear + 1, 3, 31);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("employeeId",   emp.getEmployeeId());
        result.put("employeeName", emp.getFullName());
        result.put("employeeType", emp.getEmployeeType().name());
        result.put("fyYear",       fyYear);

        // All leave types that an employee can self-serve
        List<LeaveType> trackable = List.of(
            LeaveType.CASUAL,
            LeaveType.SICK,
            LeaveType.EARNED,
            LeaveType.WFH,
            LeaveType.BEREAVEMENT,
            LeaveType.MARRIAGE,
            LeaveType.MATERNITY,
            LeaveType.PATERNITY,
            LeaveType.OPTIONAL_HOLIDAY,
            LeaveType.LOP,
            // Legacy
            LeaveType.PLANNED,
            LeaveType.SICK_LEGACY
        );

        for (LeaveType lt : trackable) {
            int total = leavePolicyService.getLeaveDays(emp.getEmployeeType(), lt);
            if (total == 0) continue; // skip unconfigured types

            long used = leaveRequestRepository.countApprovedLeavesByType(
                emp.getId(), lt.name(), fyStart, fyEnd);

            String key = lt.name().toLowerCase();
            result.put(key + "Total",     total);
            result.put(key + "Used",      used);
            result.put(key + "Remaining", Math.max(total - used, 0));
        }

        return result;
    }

    // ── Balance Report ────────────────────────────────────────────────────────

    public List<Map<String, Object>> getLeaveBalanceReport() {
        List<Employee> employees = employeeRepository
            .findByEmploymentStatusAndDeletedFalse(
                EmploymentStatus.ACTIVE,
                PageRequest.of(0, Integer.MAX_VALUE))
            .getContent();

        List<Map<String, Object>> report = new ArrayList<>();

        for (Employee emp : employees) {
            int fyYear = getCurrentFYYear();
            LocalDate fyStart = LocalDate.of(fyYear, 4, 1);
            LocalDate fyEnd   = LocalDate.of(fyYear + 1, 3, 31);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("employeeId",   emp.getEmployeeId());
            row.put("employeeName", emp.getFullName());
            row.put("department",   emp.getDepartment());
            row.put("employeeType", emp.getEmployeeType().name());

            for (LeaveType lt : LeaveType.values()) {
                if (lt.isCompanyHoliday()) continue; // not tracked per-employee
                int total = leavePolicyService.getLeaveDays(emp.getEmployeeType(), lt);
                if (total == 0) continue;
                long used = leaveRequestRepository.countApprovedLeavesByType(
                    emp.getId(), lt.name(), fyStart, fyEnd);
                String key = lt.name().toLowerCase();
                row.put(key + "Opening",  total);
                row.put(key + "Availed",  used);
                row.put(key + "Closing",  Math.max(total - used, 0));
            }

            report.add(row);
        }

        return report;
    }

    public List<Map<String, Object>> getLeaveBalanceReport(
            String name, String employeeId, String department, String employeeType) {

        return getLeaveBalanceReport().stream()
            .filter(r -> isBlankOrContains(name,         r.get("employeeName")))
            .filter(r -> isBlankOrContains(employeeId,   r.get("employeeId")))
            .filter(r -> isBlankOrContains(department,   r.get("department")))
            .filter(r -> isBlankOrEquals(employeeType,   r.get("employeeType")))
            .toList();
    }

    // ── Paged Queries ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponseDTO<LeaveResponseDTO> getLeavesByEmployee(Long empId, int page, int size) {
        Page<LeaveRequest> result = leaveRequestRepository
            .findByEmployeeId(empId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PageResponseDTO.from(result.map(this::toDTO));
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<LeaveResponseDTO> getPendingLeaves(int page, int size, Employee user) {
        if (isManager(user)) {
            return getPendingLeavesForManager(user.getEmployeeId(), page, size);
        }
        Page<LeaveRequest> result = leaveRequestRepository
            .findByStatus(LeaveStatus.PENDING,
                PageRequest.of(page, size, Sort.by("createdAt").ascending()));
        return PageResponseDTO.from(result.map(this::toDTO));
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<LeaveResponseDTO> getPendingLeavesForManager(
            String managerEmployeeId, int page, int size) {

        Employee manager = employeeRepository
            .findByEmployeeIdAndDeletedFalse(managerEmployeeId)
            .orElseThrow(() -> new RuntimeException("Manager not found"));

        List<Long> reporteeIds = employeeRepository
            .findByManagerIdAndDeletedFalse(manager.getId(), Pageable.unpaged())
            .getContent()
            .stream()
            .map(Employee::getId)
            .toList();

        if (reporteeIds.isEmpty()) return PageResponseDTO.from(Page.empty());

        Page<LeaveRequest> result = leaveRequestRepository
            .findByStatusAndEmployeeIdIn(
                LeaveStatus.PENDING,
                reporteeIds,
                PageRequest.of(page, size, Sort.by("createdAt").ascending()));

        return PageResponseDTO.from(result.map(this::toDTO));
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<LeaveResponseDTO> getAllLeaves(
            LeaveStatus status, String leaveType, String employeeName,
            int page, int size, Employee user) {

        if (isManager(user)) {
            List<Long> reporteeIds = employeeRepository
                .findByManagerIdAndDeletedFalse(user.getId(), Pageable.unpaged())
                .getContent().stream().map(Employee::getId).toList();

            if (reporteeIds.isEmpty()) return PageResponseDTO.from(Page.empty());

            Page<LeaveRequest> result = leaveRequestRepository.findWithFiltersAndEmployeeIds(
                    status, leaveType, employeeName, reporteeIds,
                    PageRequest.of(page, size, Sort.by("createdAt").descending()));

            return PageResponseDTO.from(result.map(this::toDTO));
        }

        Page<LeaveRequest> result = leaveRequestRepository.findWithFilters(
                status, leaveType, employeeName,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        return PageResponseDTO.from(result.map(this::toDTO));
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private void validateLeaveEligibility(Employee emp, LeaveType leaveType,
                                          LocalDate start, LocalDate end) {

        if (start == null || end == null || end.isBefore(start)) {
            throw new IllegalArgumentException("Invalid leave date range");
        }

        long requestedDays = ChronoUnit.DAYS.between(start, end) + 1;
        EmployeeType empType = emp.getEmployeeType();

        switch (leaveType) {

            case MATERNITY -> {
                if (!"FEMALE".equalsIgnoreCase(emp.getGender())) {
                    throw new IllegalArgumentException("Maternity leave is only for female employees");
                }
                checkEntitlement(empType, leaveType, requestedDays);
            }

            case PATERNITY -> {
                if ("FEMALE".equalsIgnoreCase(emp.getGender())) {
                    throw new IllegalArgumentException("Paternity leave is only for male employees");
                }
                checkEntitlement(empType, leaveType, requestedDays);
            }

            case EARNED -> {
                // Only 3 consecutive days as EL; rest are LOP
                int maxConsecutive = leavePolicyService.getMaxConsecutiveDays(empType, leaveType);
                if (maxConsecutive > 0 && requestedDays > maxConsecutive) {
                    throw new IllegalArgumentException(
                        "Earned Leave allows max " + maxConsecutive
                        + " consecutive days. Please apply LOP for the remaining "
                        + (requestedDays - maxConsecutive) + " day(s).");
                }
                checkEntitlement(empType, leaveType, requestedDays);
            }

            case LOP -> {
                // LOP is always allowed — no entitlement check needed
            }

            case PUBLIC_HOLIDAY, OPTIONAL_HOLIDAY -> {
                // Company-declared — no entitlement check, admin-managed
            }

            case WFH, CASUAL, SICK, BEREAVEMENT, MARRIAGE, SICK_LEGACY, PLANNED -> {
                if (empType != null) {
                    checkEntitlement(empType, leaveType, requestedDays);
                }
            }
        }
    }

    private void checkEntitlement(EmployeeType empType, LeaveType leaveType, long requested) {
        int entitlement = leavePolicyService.getLeaveDays(empType, leaveType);
        if (entitlement <= 0) {
            throw new IllegalArgumentException(
                "Leave policy not configured for " + empType + " and " + leaveType.getDisplayName());
        }
        // Note: balance (used vs remaining) checked separately in LeaveBalanceService
    }

    // ── DTO Mapping ───────────────────────────────────────────────────────────

    private LeaveResponseDTO toDTO(LeaveRequest r) {
        LeaveResponseDTO dto = new LeaveResponseDTO();
        dto.setId(r.getId());
        dto.setEmployeeId(r.getEmployee().getEmployeeId());
        dto.setEmployeeName(r.getEmployee().getFullName());
        dto.setLeaveType(r.getLeaveType());
        dto.setStartDate(r.getStartDate());
        dto.setEndDate(r.getEndDate());
        dto.setTotalDays(r.getLeaveDays());
        dto.setPaidDays(r.getPaidDays());
        dto.setUnpaidDays(r.getUnpaidDays());
        dto.setStatus(r.getStatus());
        dto.setReason(r.getReason());
        dto.setRejectionReason(r.getRejectionReason());
        dto.setApprovedBy(r.getApprovedBy());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }

    // ── Filter helpers ────────────────────────────────────────────────────────

    private boolean isBlankOrContains(String filter, Object value) {
        if (filter == null || filter.isEmpty()) return true;
        return value != null && value.toString().toLowerCase().contains(filter.toLowerCase());
    }

    private boolean isBlankOrEquals(String filter, Object value) {
        if (filter == null || filter.isEmpty()) return true;
        return value != null && value.toString().equalsIgnoreCase(filter);
    }
}
