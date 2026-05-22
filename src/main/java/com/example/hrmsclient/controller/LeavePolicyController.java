package com.example.hrmsclient.controller;

import com.example.hrmsclient.dto.ApiResponse;
import com.example.hrmsclient.entity.LeavePolicy;
import com.example.hrmsclient.entity.LeaveType;
import com.example.hrmsclient.service.LeavePolicyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave-policy")
public class LeavePolicyController {

    private final LeavePolicyService service;

    public LeavePolicyController(LeavePolicyService service) {
        this.service = service;
    }

    // ── List all configured policies ──────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<LeavePolicy>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // ── List all available leave types (for dropdowns) ────────────────────
    @GetMapping("/leave-types")
    public ResponseEntity<List<Map<String, String>>> getLeaveTypes() {
        List<Map<String, String>> types = Arrays.stream(LeaveType.values())
            .map(lt -> Map.of(
                "code",        lt.name(),
                "displayName", lt.getDisplayName(),
                "paid",        String.valueOf(lt.isPaid()),
                "companyHoliday", String.valueOf(lt.isCompanyHoliday())
            ))
            .toList();
        return ResponseEntity.ok(types);
    }

    // ── Get by ID ─────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<LeavePolicy> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // ── Create ────────────────────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<LeavePolicy> create(@RequestBody LeavePolicy policy) {
        return ResponseEntity.ok(service.create(policy));
    }

    // ── Update ────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<LeavePolicy> update(
            @PathVariable Long id,
            @RequestBody LeavePolicy policy) {
        return ResponseEntity.ok(service.update(id, policy));
    }

    // ── Delete ────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Policy deleted successfully"));
    }
}
