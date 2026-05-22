package com.example.hrmsclient.service;

import com.example.hrmsclient.entity.*;
import com.example.hrmsclient.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class ShiftService {

    private final ShiftRepository           shiftRepo;
    private final ShiftAssignmentRepository assignmentRepo;
    private final EmployeeRepository        employeeRepo;

    public ShiftService(ShiftRepository shiftRepo,
                        ShiftAssignmentRepository assignmentRepo,
                        EmployeeRepository employeeRepo) {
        this.shiftRepo     = shiftRepo;
        this.assignmentRepo = assignmentRepo;
        this.employeeRepo  = employeeRepo;
    }

    // ── Shifts ────────────────────────────────────────────────────────────────

    @Transactional
    public Shift createShift(Shift shift) {
        return shiftRepo.save(shift);
    }

    @Transactional
    public Shift updateShift(Long id, Shift updated) {
        Shift existing = shiftRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Shift not found: " + id));
        existing.setName(updated.getName());
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());
        existing.setGracePeriodMinutes(updated.getGracePeriodMinutes());
        existing.setNightShift(updated.isNightShift());
        existing.setDescription(updated.getDescription());
        return shiftRepo.save(existing);
    }

    public List<Shift> getAllShifts() {
        return shiftRepo.findAll();
    }

    public List<Shift> getActiveShifts() {
        return shiftRepo.findByStatus("ACTIVE");
    }

    // ── Assignments ───────────────────────────────────────────────────────────

    @Transactional
    public ShiftAssignment assignShift(Long employeeId, Long shiftId,
                                       LocalDate from, LocalDate to,
                                       String assignedBy, String remarks) {
        Employee emp = employeeRepo.findByIdAndDeletedFalse(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));
        Shift shift = shiftRepo.findById(shiftId)
            .orElseThrow(() -> new RuntimeException("Shift not found: " + shiftId));

        // Cancel any existing active assignment for overlap period
        assignmentRepo.findByEmployeeIdAndStatus(employeeId, "ACTIVE")
            .forEach(a -> {
                if (a.getEffectiveTo() == null || a.getEffectiveTo().isAfter(from.minusDays(1))) {
                    a.setStatus("CANCELLED");
                    a.setEffectiveTo(from.minusDays(1));
                    assignmentRepo.save(a);
                }
            });

        ShiftAssignment sa = new ShiftAssignment();
        sa.setEmployee(emp);
        sa.setShift(shift);
        sa.setEffectiveFrom(from);
        sa.setEffectiveTo(to);
        sa.setAssignedBy(assignedBy);
        sa.setRemarks(remarks);
        sa.setStatus("ACTIVE");
        return assignmentRepo.save(sa);
    }

    /**
     * Bulk assign the same shift to multiple employees.
     */
    @Transactional
    public List<ShiftAssignment> bulkAssign(List<Long> employeeIds, Long shiftId,
                                             LocalDate from, LocalDate to,
                                             String assignedBy) {
        List<ShiftAssignment> result = new ArrayList<>();
        for (Long empId : employeeIds) {
            try {
                result.add(assignShift(empId, shiftId, from, to, assignedBy, "Bulk assignment"));
            } catch (Exception ex) {
                // skip and continue
            }
        }
        return result;
    }

    @Transactional
    public void cancelAssignment(Long assignmentId) {
        ShiftAssignment sa = assignmentRepo.findById(assignmentId)
            .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));
        sa.setStatus("CANCELLED");
        assignmentRepo.save(sa);
    }

    /**
     * Get current shift for an employee on a date.
     */
    public Optional<ShiftAssignment> getCurrentShift(Long employeeId, LocalDate date) {
        return assignmentRepo.findActiveForEmployeeOnDate(employeeId, date);
    }

    public List<ShiftAssignment> getEmployeeShiftHistory(Long employeeId) {
        return assignmentRepo.findByEmployeeIdAndStatus(employeeId, "ACTIVE");
    }

    public List<ShiftAssignment> getShiftRoster(Long shiftId) {
        return assignmentRepo.findByShiftIdAndStatus(shiftId, "ACTIVE");
    }
}