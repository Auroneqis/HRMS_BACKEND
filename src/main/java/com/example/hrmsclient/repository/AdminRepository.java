package com.example.hrmsclient.repository;

import com.example.hrmsclient.entity.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    // ── Auth ──────────────────────────────────────────────────────────────────
    Optional<Admin> findByEmailId(String emailId);
    boolean existsByEmailId(String emailId);
    boolean existsByEmailIdAndDeletedFalse(String emailId);

    // ── Find ──────────────────────────────────────────────────────────────────
    Optional<Admin> findByAdminId(String adminId);
    Optional<Admin> findByIdAndDeletedFalse(Long id);
    boolean existsByAdminId(String adminId);

    // ── All non-deleted ───────────────────────────────────────────────────────
    Page<Admin> findAllByDeletedFalse(Pageable pageable);
    Page<Admin> findByDeletedFalse(Pageable pageable);

    // ── Filter by active ──────────────────────────────────────────────────────
    Page<Admin> findByActiveAndDeletedFalse(boolean active, Pageable pageable);

    // ── Filter by role ────────────────────────────────────────────────────────
    Page<Admin> findByRoleAndDeletedFalse(String role, Pageable pageable);
    Page<Admin> findByRoleIgnoreCaseAndDeletedFalse(String role, Pageable pageable);

    // ── Filter by department ──────────────────────────────────────────────────
    Page<Admin> findByDepartmentIgnoreCaseAndDeletedFalse(String dept, Pageable pageable);

    // ── Search across all fields ──────────────────────────────────────────────
    @Query("SELECT a FROM Admin a WHERE a.deleted = false AND " +
           "(LOWER(a.firstName)  LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(a.lastName)   LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(a.emailId)    LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(a.adminId)    LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(a.department) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Admin> searchAdmins(@Param("q") String query, Pageable pageable);

    // ── Departments ───────────────────────────────────────────────────────────
    @Query("SELECT DISTINCT a.department FROM Admin a " +
           "WHERE a.deleted = false ORDER BY a.department")
    List<String> findAllDepartments();

    // ── Soft Delete ───────────────────────────────────────────────────────────
    @Modifying
    @Transactional
    @Query("UPDATE Admin a SET a.deleted = true, a.deletedAt = :deletedAt, " +
           "a.active = false WHERE a.id = :id AND a.deleted = false")
    int softDeleteById(@Param("id") Long id,
                       @Param("deletedAt") LocalDateTime deletedAt);

    // ── Counts ────────────────────────────────────────────────────────────────
    long countByDeletedFalse();
    long countByActiveAndDeletedFalse(boolean active);
    long countByRoleAndDeletedFalse(String role);

    @Query("SELECT a.role, COUNT(a) FROM Admin a " +
           "WHERE a.deleted = false GROUP BY a.role")
    List<Object[]> countByRole();

    @Query("SELECT a.department, COUNT(a) FROM Admin a " +
           "WHERE a.deleted = false GROUP BY a.department")
    List<Object[]> countByDepartment();
 
   
}