
package com.example.hrmsclient.repository;
import com.example.hrmsclient.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
 
    List<ShiftAssignment> findByEmployeeIdAndStatus(Long employeeId, String status);
 
    // Active assignment on a specific date
    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.employee.id = :empId " +
           "AND sa.status = 'ACTIVE' " +
           "AND sa.effectiveFrom <= :date " +
           "AND (sa.effectiveTo IS NULL OR sa.effectiveTo >= :date)")
    Optional<ShiftAssignment> findActiveForEmployeeOnDate(
        @Param("empId") Long empId, @Param("date") LocalDate date);
 
    List<ShiftAssignment> findByShiftIdAndStatus(Long shiftId, String status);
}
 