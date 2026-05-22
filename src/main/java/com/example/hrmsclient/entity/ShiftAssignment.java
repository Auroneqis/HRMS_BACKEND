package com.example.hrmsclient.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Assigns a specific shift to an employee for a date range.
 */
@Entity
@Table(name = "shift_assignments",
    indexes = {
        @Index(name = "idx_sa_emp",   columnList = "employee_id"),
        @Index(name = "idx_sa_shift", columnList = "shift_id"),
        @Index(name = "idx_sa_dates", columnList = "effectiveFrom, effectiveTo")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class ShiftAssignment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;   // null = indefinite

    @Column(length = 20)
    private String status = "ACTIVE"; // ACTIVE | CANCELLED

    @Column(length = 100)
    private String assignedBy;

    @Column(length = 300)
    private String remarks;

    @CreatedDate @Column(updatable = false)
    private LocalDateTime createdAt;

    // Getters & Setters
    public Long getId()                           { return id; }
    public void setId(Long id)                    { this.id = id; }
    public Employee getEmployee()                 { return employee; }
    public void setEmployee(Employee v)           { this.employee = v; }
    public Shift getShift()                       { return shift; }
    public void setShift(Shift v)                 { this.shift = v; }
    public LocalDate getEffectiveFrom()           { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate v)     { this.effectiveFrom = v; }
    public LocalDate getEffectiveTo()             { return effectiveTo; }
    public void setEffectiveTo(LocalDate v)       { this.effectiveTo = v; }
    public String getStatus()                     { return status; }
    public void setStatus(String v)               { this.status = v; }
    public String getAssignedBy()                 { return assignedBy; }
    public void setAssignedBy(String v)           { this.assignedBy = v; }
    public String getRemarks()                    { return remarks; }
    public void setRemarks(String v)              { this.remarks = v; }
    public LocalDateTime getCreatedAt()           { return createdAt; }
    public void setCreatedAt(LocalDateTime v)     { this.createdAt = v; }
}