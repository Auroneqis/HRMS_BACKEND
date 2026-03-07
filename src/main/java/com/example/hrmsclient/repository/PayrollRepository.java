package com.example.hrmsclient.repository;

import com.example.hrmsclient.entity.Payroll;
import com.example.hrmsclient.entity.PayrollStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    // Check if payroll already generated for employee this month
    @Query("SELECT COUNT(p) > 0 FROM Payroll p WHERE p.employee.id = :empId AND p.payrollMonth = :month")
    boolean existsByEmployeeIdAndPayrollMonth(
            @Param("empId") Long empId,
            @Param("month") LocalDate month);

    @Query("SELECT p FROM Payroll p WHERE p.employee.id = :empId AND p.payrollMonth = :month")
    Optional<Payroll> findByEmployeeIdAndPayrollMonth(
            @Param("empId") Long empId,
            @Param("month") LocalDate month);

    // Get all payrolls for one employee
    @Query("SELECT p FROM Payroll p WHERE p.employee.id = :empId ORDER BY p.payrollMonth DESC")
    List<Payroll> findByEmployeeId(@Param("empId") Long empId);

    // Get all payrolls by month (for bulk processing)
    @Query("SELECT p FROM Payroll p WHERE p.payrollMonth = :month ORDER BY p.employee.firstName")
    Page<Payroll> findByPayrollMonth(@Param("month") LocalDate month, Pageable pageable);

    // Get all payrolls by month + status
    @Query("SELECT p FROM Payroll p WHERE p.payrollMonth = :month AND p.status = :status")
    List<Payroll> findByPayrollMonthAndStatus(
            @Param("month") LocalDate month,
            @Param("status") PayrollStatus status);

    // Get all APPROVED payrolls — ready for payment
    List<Payroll> findByStatus(PayrollStatus status);

    // Total payroll cost for a month
    @Query("SELECT SUM(p.netSalary) FROM Payroll p WHERE p.payrollMonth = :month AND p.status = 'PAID'")
    BigDecimal getTotalNetSalaryByMonth(@Param("month") LocalDate month);

    // Count by status for a month
    @Query("SELECT COUNT(p) FROM Payroll p WHERE p.payrollMonth = :month AND p.status = :status")
    long countByMonthAndStatus(
            @Param("month") LocalDate month,
            @Param("status") PayrollStatus status);
    Page<Payroll> findByPayrollMonthAndStatus(
            LocalDate month, PayrollStatus status, Pageable pageable);
}