package com.example.hrmsclient.service;

import com.example.hrmsclient.dto.AdminRequestDTO;
import com.example.hrmsclient.dto.AdminResponseDTO;
import com.example.hrmsclient.entity.Admin;
import com.example.hrmsclient.repository.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(AdminRepository adminRepository,
                        PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ── REGISTER ──────────────────────────────────────────────────────────────
    @Transactional
    public AdminResponseDTO registerAdmin(AdminRequestDTO req) {
        if (adminRepository.existsByEmailIdAndDeletedFalse(req.getEmailId()))
            throw new RuntimeException("Email already exists: " + req.getEmailId());

        Admin admin = new Admin();
        admin.setAdminId(generateAdminId());
        admin.setFirstName(req.getFirstName());
        admin.setLastName(req.getLastName());
        admin.setEmailId(req.getEmailId());
        admin.setPassword(passwordEncoder.encode(req.getPassword()));
        admin.setPhone(req.getPhone());
        admin.setProfilePhotoUrl(req.getProfilePhotoUrl());
        admin.setRole(req.getRole() != null ? req.getRole() : "ADMIN");
        admin.setDepartment(req.getDepartment());
        admin.setDesignation(req.getDesignation());
        if (hasValue(req.getReportingManager()))
            admin.setReportingManager(req.getReportingManager().trim());

        return toDTO(adminRepository.save(admin));
    }

    // ── GET ALL WITH FILTERS ──────────────────────────────────────────────────
    public Page<AdminResponseDTO> getAllAdmins(
            String search, String role, String department,
            Boolean active, int page, int size,
            String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Admin> result;

        if (hasValue(search))     result = adminRepository.searchAdmins(search.trim(), pageable);
        else if (hasValue(role))  result = adminRepository.findByRoleIgnoreCaseAndDeletedFalse(role, pageable);
        else if (hasValue(department)) result = adminRepository.findByDepartmentIgnoreCaseAndDeletedFalse(department, pageable);
        else if (active != null)  result = adminRepository.findByActiveAndDeletedFalse(active, pageable);
        else                      result = adminRepository.findByDeletedFalse(pageable);

        return result.map(this::toDTO);
    }

    // ── GET BY ID ─────────────────────────────────────────────────────────────
    public AdminResponseDTO getAdminById(Long id) {
        return toDTO(adminRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Admin not found: " + id)));
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    @Transactional
    public AdminResponseDTO updateAdmin(Long id, AdminRequestDTO req) {
        Admin admin = adminRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Admin not found: " + id));

        if (hasValue(req.getFirstName()))        admin.setFirstName(req.getFirstName());
        if (hasValue(req.getLastName()))         admin.setLastName(req.getLastName());
        if (hasValue(req.getPhone()))            admin.setPhone(req.getPhone());
        if (hasValue(req.getProfilePhotoUrl()))  admin.setProfilePhotoUrl(req.getProfilePhotoUrl());
        if (hasValue(req.getDepartment()))       admin.setDepartment(req.getDepartment());
        if (hasValue(req.getDesignation()))      admin.setDesignation(req.getDesignation());
        if (hasValue(req.getRole()))             admin.setRole(req.getRole());
        if (hasValue(req.getPassword()))
            admin.setPassword(passwordEncoder.encode(req.getPassword()));
        if (req.getReportingManager() != null) {
            admin.setReportingManager(
                    req.getReportingManager().isBlank() ? null : req.getReportingManager().trim());
        }

        return toDTO(adminRepository.save(admin));
    }

    // ── TOGGLE ACTIVE ─────────────────────────────────────────────────────────
    @Transactional
    public AdminResponseDTO toggleActive(Long id) {
        Admin admin = adminRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Admin not found: " + id));
        admin.setActive(!admin.isActive());
        return toDTO(adminRepository.save(admin));
    }

    // ── SOFT DELETE ───────────────────────────────────────────────────────────
    @Transactional
    public void deleteAdmin(Long id) {
        int rows = adminRepository.softDeleteById(id, LocalDateTime.now());
        if (rows == 0)
            throw new RuntimeException("Admin not found or already deleted: " + id);
        log.info("Admin soft-deleted: {}", id);
    }

    // ── STATS ─────────────────────────────────────────────────────────────────
    public Map<String, Object> getDashboardStats() {
        return Map.of(
            "totalAdmins",    adminRepository.countByDeletedFalse(),
            "activeAdmins",   adminRepository.countByActiveAndDeletedFalse(true),
            "inactiveAdmins", adminRepository.countByActiveAndDeletedFalse(false),
            "superAdmins",    adminRepository.countByRoleAndDeletedFalse("SUPER_ADMIN")
        );
    }

    // ── GENERATE ADMIN ID ─────────────────────────────────────────────────────
    private String generateAdminId() {
        long count = adminRepository.count() + 1;
        return "ADM" + String.format("%03d", count);
    }

    // ── Entity → DTO ──────────────────────────────────────────────────────────
    private AdminResponseDTO toDTO(Admin a) {
        AdminResponseDTO dto = new AdminResponseDTO();
        dto.setId(a.getId());
        dto.setAdminId(a.getAdminId());
        dto.setFullName(a.getFullName());
        dto.setFirstName(a.getFirstName());
        dto.setLastName(a.getLastName());
        dto.setEmailId(a.getEmailId());
        dto.setPhone(a.getPhone());
        dto.setProfilePhotoUrl(a.getProfilePhotoUrl());
        dto.setRole(a.getRole());
        dto.setDepartment(a.getDepartment());
        dto.setDesignation(a.getDesignation());
        dto.setReportingManager(a.getReportingManager());
        dto.setActive(a.isActive());
        dto.setLastLoginAt(a.getLastLoginAt());
        dto.setCreatedAt(a.getCreatedAt());
        dto.setUpdatedAt(a.getUpdatedAt());
        return dto;
    }

    private boolean hasValue(String s) { return s != null && !s.isBlank(); }
}