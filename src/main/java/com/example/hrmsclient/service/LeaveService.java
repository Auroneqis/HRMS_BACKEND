package com.example.hrmsclient.service;

import com.example.hrmsclient.dto.*;
import com.example.hrmsclient.entity.*;
import com.example.hrmsclient.repository.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class LeaveService {

    private final LeaveRequestRepository leaveRepo;
    private final EmployeeRepository employeeRepo;
    private final HrmsEmailService hrmsEmailService;

    public LeaveService(LeaveRequestRepository leaveRepo,
                        EmployeeRepository employeeRepo,
                        HrmsEmailService hrmsEmailService) {
        this.leaveRepo        = leaveRepo;
        this.employeeRepo     = employeeRepo;
        this.hrmsEmailService = hrmsEmailService;
    }

    @Transactional
    public LeaveResponseDTO applyLeave(LeaveRequestDTO dto) {
        Employee employee = employeeRepo.findByIdAndDeletedFalse(dto.getEmployeeId())
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (dto.getEndDate().isBefore(dto.getStartDate()))
            throw new IllegalArgumentException("End date cannot be before start date");

        if (leaveRepo.hasOverlappingLeave(dto.getEmployeeId(), dto.getStartDate(), dto.getEndDate()))
            throw new IllegalArgumentException("Overlapping leave request exists");

  
        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(employee);
        leave.setLeaveType(dto.getLeaveType());
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        leave.setReason(dto.getReason());
        leave.setStatus(LeaveStatus.PENDING);

        LeaveRequest saved = leaveRepo.save(leave);
        hrmsEmailService.sendLeaveAppliedEmail(saved);
        return toDto(saved);
    }

    @Transactional
    public LeaveResponseDTO approveLeave(Long leaveId) {
        LeaveRequest leave = leaveRepo.findById(leaveId)
            .orElseThrow(() -> new RuntimeException("Leave not found"));

        if (leave.getStatus() != LeaveStatus.PENDING)
            throw new IllegalStateException("Leave is not in PENDING state");

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApprovedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        leave.setApprovedAt(LocalDateTime.now());

        LeaveRequest saved = leaveRepo.save(leave);
        hrmsEmailService.sendLeaveApprovedEmail(saved);
        return toDto(saved);
    }

    @Transactional
    public LeaveResponseDTO rejectLeave(Long leaveId, String reason) {
        LeaveRequest leave = leaveRepo.findById(leaveId)
            .orElseThrow(() -> new RuntimeException("Leave not found"));

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setRejectionReason(reason);

        LeaveRequest saved = leaveRepo.save(leave);
        hrmsEmailService.sendLeaveRejectedEmail(saved);
        return toDto(saved);
    }

    public PageResponseDTO<LeaveResponseDTO> getLeavesByEmployee(Long empId, int page, int size) {
        Page<LeaveRequest> result = leaveRepo.findByEmployee_IdOrderByCreatedAtDesc(
            empId, PageRequest.of(page, size));
        return PageResponseDTO.from(result.map(this::toDto));
    }

    public PageResponseDTO<LeaveResponseDTO> getPendingLeaves(int page, int size) {
        Page<LeaveRequest> result = leaveRepo.findByStatusOrderByCreatedAtDesc(
            LeaveStatus.PENDING, PageRequest.of(page, size));
        return PageResponseDTO.from(result.map(this::toDto));
    }

    private LeaveResponseDTO toDto(LeaveRequest l) {
        return new LeaveResponseDTO(
            l.getId(),
            l.getEmployee().getEmployeeId(),
            l.getEmployee().getFullName(),
            l.getLeaveType(),
            l.getStartDate(),
            l.getEndDate(),
            l.getLeaveDays(),
            l.getStatus(),
            l.getReason(),
            l.getRejectionReason(),
            l.getApprovedBy(),
            l.getApprovedAt(),
            l.getCreatedAt()
        );
    }
}