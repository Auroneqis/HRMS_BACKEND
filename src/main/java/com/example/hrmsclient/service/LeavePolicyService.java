package com.example.hrmsclient.service;

import com.example.hrmsclient.entity.EmployeeType;
import com.example.hrmsclient.entity.LeavePolicy;
import com.example.hrmsclient.entity.LeaveType;
import com.example.hrmsclient.repository.LeavePolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LeavePolicyService {

    private final LeavePolicyRepository repo;

    public LeavePolicyService(LeavePolicyRepository repo) {
        this.repo = repo;
    }

    // ── CREATE ────────────────────────────────────────────────────────────

    public LeavePolicy create(LeavePolicy policy) {
        if (repo.existsByEmployeeTypeAndLeaveType(
                policy.getEmployeeType(), policy.getLeaveType())) {
            throw new IllegalArgumentException(
                "Policy already exists for "
                + policy.getEmployeeType() + " - " + policy.getLeaveType());
        }
        return repo.save(policy);
    }

    // ── READ ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<LeavePolicy> getAll() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public LeavePolicy getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found: " + id));
    }

    // ── UPDATE ────────────────────────────────────────────────────────────

    public LeavePolicy update(Long id, LeavePolicy updated) {
        LeavePolicy existing = getById(id);
        existing.setTotalDays(updated.getTotalDays());
        existing.setMaxConsecutiveDays(updated.getMaxConsecutiveDays());
        existing.setCarryForward(updated.isCarryForward());
        existing.setDescription(updated.getDescription());
        return repo.save(existing);
    }

    // ── DELETE ────────────────────────────────────────────────────────────

    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Policy not found: " + id);
        }
        repo.deleteById(id);
    }

    // ── QUERY HELPERS ─────────────────────────────────────────────────────

    /**
     * Returns total days for the given employee type + leave type.
     * Accepts both the new {@link LeaveType} enum and legacy string values
     * ("Planned", "Sick") by resolving through {@link LeaveType#fromString}.
     *
     * @param type       employee category
     * @param leaveType  enum constant or legacy string
     * @return total days configured, or 0 if not found
     */
    @Transactional(readOnly = true)
    public int getLeaveDays(EmployeeType type, String leaveType) {
        try {
            LeaveType lt = LeaveType.fromString(leaveType);
            return getLeaveDays(type, lt);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public int getLeaveDays(EmployeeType type, LeaveType leaveType) {
        return repo.findByEmployeeTypeAndLeaveType(type, leaveType)
                .map(LeavePolicy::getTotalDays)
                .orElse(0);
    }

    /**
     * Returns the max consecutive days allowed for a leave type + employee type.
     * 0 means no consecutive-day limit is enforced.
     */
    @Transactional(readOnly = true)
    public int getMaxConsecutiveDays(EmployeeType type, LeaveType leaveType) {
        return repo.findByEmployeeTypeAndLeaveType(type, leaveType)
                .map(LeavePolicy::getMaxConsecutiveDays)
                .orElse(0);
    }
}
