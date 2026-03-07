package com.example.hrmsclient.controller;

import com.example.hrmsclient.dto.AdminRequestDTO;
import com.example.hrmsclient.dto.AdminResponseDTO;
import com.example.hrmsclient.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ── REGISTER — Public ─────────────────────────────────────────────────────
    // POST /api/admin/register
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AdminRequestDTO req) {
        return ResponseEntity.ok(Map.of(
            "status",  "success",
            "message", "Admin registered successfully",
            "data",    adminService.registerAdmin(req)
        ));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAll(
            @RequestParam(required = false)           String  search,
            @RequestParam(required = false)           String  role,
            @RequestParam(required = false)           String  department,
            @RequestParam(required = false)           Boolean active,
            @RequestParam(defaultValue = "0")         int     page,
            @RequestParam(defaultValue = "10")        int     size,
            @RequestParam(defaultValue = "firstName") String  sortBy,
            @RequestParam(defaultValue = "asc")       String  sortDir) {

        Page<AdminResponseDTO> admins = adminService.getAllAdmins(
                search, role, department, active, page, size, sortBy, sortDir);

        return ResponseEntity.ok(Map.of(
            "status",       "success",
            "data",         admins.getContent(),
            "totalRecords", admins.getTotalElements(),
            "totalPages",   admins.getTotalPages(),
            "currentPage",  admins.getNumber()
        ));
    }

    // GET /api/admin/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "data",   adminService.getAdminById(id)
        ));
    }

    // ── UPDATE — ADMIN only ───────────────────────────────────────────────────
    // PUT /api/admin/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody AdminRequestDTO req) {
        return ResponseEntity.ok(Map.of(
            "status",  "success",
            "message", "Admin updated successfully",
            "data",    adminService.updateAdmin(id, req)
        ));
    }

    // ── TOGGLE ACTIVE/INACTIVE — ADMIN only ───────────────────────────────────
    // PATCH /api/admin/{id}/toggle-active
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleActive(@PathVariable Long id) {
        AdminResponseDTO admin = adminService.toggleActive(id);
        return ResponseEntity.ok(Map.of(
            "status",  "success",
            "message", "Admin is now: " + (admin.isActive() ? "ACTIVE" : "INACTIVE"),
            "data",    admin
        ));
    }

    // ── SOFT DELETE — ADMIN only
    // DELETE /api/admin/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.ok(Map.of(
            "status",  "success",
            "message", "Admin deleted successfully"
        ));
    }

    // ── STATS — ADMIN only ────────────────────────────────────────────────────
    // GET /api/admin/stats
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "data",   adminService.getDashboardStats()
        ));
    }
}
