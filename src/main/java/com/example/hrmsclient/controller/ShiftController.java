package com.example.hrmsclient.controller;

import com.example.hrmsclient.entity.Shift;
import com.example.hrmsclient.entity.ShiftAssignment;
import com.example.hrmsclient.service.ShiftService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * BASE PATH: /api/shifts
 *
 * GET    /api/shifts                             — list all shifts
 * POST   /api/shifts                             — create shift
 * PUT    /api/shifts/{id}                        — update shift
 * POST   /api/shifts/assign                      — assign shift to employee
 * POST   /api/shifts/bulk-assign                 — assign shift to multiple employees
 * DELETE /api/shifts/assignments/{id}            — cancel assignment
 * GET    /api/shifts/employee/{empId}            — employee's current shift
 * GET    /api/shifts/{shiftId}/roster            — all employees on a shift
 */
@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public ResponseEntity<?> listShifts() {
        return ResponseEntity.ok(Map.of("status", "success", "data", shiftService.getActiveShifts()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> createShift(@RequestBody Shift shift) {
        return ResponseEntity.ok(Map.of("status", "success", "data", shiftService.createShift(shift)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> updateShift(@PathVariable Long id, @RequestBody Shift shift) {
        return ResponseEntity.ok(Map.of("status", "success", "data", shiftService.updateShift(id, shift)));
    }

    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> assignShift(@RequestBody Map<String, Object> body,
                                         @AuthenticationPrincipal UserDetails user) {
        Long employeeId = Long.valueOf(body.get("employeeId").toString());
        Long shiftId    = Long.valueOf(body.get("shiftId").toString());
        LocalDate from  = LocalDate.parse(body.get("effectiveFrom").toString());
        LocalDate to    = body.containsKey("effectiveTo")
                          ? LocalDate.parse(body.get("effectiveTo").toString()) : null;
        String remarks  = (String) body.getOrDefault("remarks", "");

        ShiftAssignment sa = shiftService.assignShift(employeeId, shiftId, from, to,
                                                       user.getUsername(), remarks);
        return ResponseEntity.ok(Map.of("status", "success", "data", sa));
    }

    @PostMapping("/bulk-assign")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> bulkAssign(@RequestBody Map<String, Object> body,
                                        @AuthenticationPrincipal UserDetails user) {
        List<Integer> ids  = (List<Integer>) body.get("employeeIds");
        Long shiftId       = Long.valueOf(body.get("shiftId").toString());
        LocalDate from     = LocalDate.parse(body.get("effectiveFrom").toString());
        LocalDate to       = body.containsKey("effectiveTo")
                             ? LocalDate.parse(body.get("effectiveTo").toString()) : null;
        List<Long> empIds  = ids.stream().map(Integer::longValue).toList();

        List<ShiftAssignment> result = shiftService.bulkAssign(empIds, shiftId, from, to, user.getUsername());
        return ResponseEntity.ok(Map.of("status", "success", "assigned", result.size()));
    }

    @DeleteMapping("/assignments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> cancelAssignment(@PathVariable Long id) {
        shiftService.cancelAssignment(id);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Assignment cancelled"));
    }

    @GetMapping("/employee/{empId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public ResponseEntity<?> getEmployeeShift(
            @PathVariable Long empId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "data",   shiftService.getCurrentShift(empId, targetDate)
        ));
    }

    @GetMapping("/{shiftId}/roster")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public ResponseEntity<?> getRoster(@PathVariable Long shiftId) {
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "data",   shiftService.getShiftRoster(shiftId)
        ));
    }
}