package com.example.hrmsclient.repository;

import com.example.hrmsclient.entity.Employee;
import com.example.hrmsclient.entity.EmploymentStatus;
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
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // ── Auth ──────────────────────────────────────────────────────────────────

    Optional<Employee> findByEmailId(String emailId);

    // Check email exists (any record)
    boolean existsByEmailId(String emailId);

    // ✅ Check email exists only among active (non-deleted) employees
    boolean existsByEmailIdAndDeletedFalse(String emailId);

    // ── Find ──────────────────────────────────────────────────────────────────

    Optional<Employee> findByIdAndDeletedFalse(Long id);

    Optional<Employee> findByEmployeeId(String employeeId);

    boolean existsByEmployeeId(String employeeId);

    boolean existsByEmployeeIdAndDeletedFalse(String employeeId);

    //  findAllByDeletedFalse — same as findByDeletedFalse, both work
    Page<Employee> findAllByDeletedFalse(Pageable pageable);

    // Also keep the shorter version
    Page<Employee> findByDeletedFalse(Pageable pageable);

    // Filter by department (case-sensitive)
    Page<Employee> findByDepartmentAndDeletedFalse(String department, Pageable pageable);

    // Filter by department (case-insensitive)
    Page<Employee> findByDepartmentIgnoreCaseAndDeletedFalse(String department, Pageable pageable);

    // Filter by employment status
    Page<Employee> findByEmploymentStatusAndDeletedFalse(EmploymentStatus status, Pageable pageable);

    // Filter by role (case-sensitive)
    Page<Employee> findByRoleAndDeletedFalse(String role, Pageable pageable);

    // Filter by role (case-insensitive)
    Page<Employee> findByRoleIgnoreCaseAndDeletedFalse(String role, Pageable pageable);

    // ── Search ────────────────────────────────────────────────────────────────

    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND " +
           "(LOWER(e.firstName)  LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(e.lastName)   LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(e.emailId)    LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(e.employeeId) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(e.department) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Employee> searchEmployees(@Param("q") String query, Pageable pageable);

    // ── Departments 

    @Query("SELECT DISTINCT e.department FROM Employee e WHERE e.deleted = false ORDER BY e.department")
    List<String> findAllDepartments();

    // ── Soft Delete

    @Modifying
    @Transactional
    @Query("UPDATE Employee e SET " +
           "e.deleted = true, " +
           "e.deletedAt = :deletedAt, " +
           "e.employmentStatus = com.example.hrmsclient.entity.EmploymentStatus.EXITED " +
           "WHERE e.id = :id AND e.deleted = false")
    int softDeleteById(@Param("id") Long id,
                       @Param("deletedAt") LocalDateTime deletedAt);

 
    long countByDeletedFalse();

    long countByEmploymentStatusAndDeletedFalse(EmploymentStatus employmentStatus);

    @Query("SELECT e.department, COUNT(e) FROM Employee e WHERE e.deleted = false GROUP BY e.department")
    List<Object[]> countByDepartment();

    @Query("SELECT e.role, COUNT(e) FROM Employee e WHERE e.deleted = false GROUP BY e.role")
    List<Object[]> countByRole();
    Optional<Employee> findByEmailIdAndDeletedFalse(String emailId); 
    
}