package com.example.hrmsclient.entity;

import jakarta.persistence.*;

/**
 * Stores per-employeeType leave entitlements.
 *
 * Updated to reference the {@link LeaveType} enum so that new leave categories
 * (CASUAL, SICK, EARNED, WFH, MATERNITY, BEREAVEMENT, MARRIAGE, LOP,
 * PUBLIC_HOLIDAY, OPTIONAL_HOLIDAY, PATERNITY) are first-class values.
 *
 * Backward-compatible: existing "Planned" / "Sick" rows still load correctly
 * via LeaveType.fromString().
 */
@Entity
@Table(name = "leave_policy",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"employee_type", "leave_type"}))
public class LeavePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_type", nullable = false, length = 30)
    private EmployeeType employeeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 30)
    private LeaveType leaveType;

    @Column(nullable = false)
    private int totalDays;

    @Column(nullable = false)
    private int maxConsecutiveDays = 0;

    /** Whether carry-forward to next year is allowed. */
    @Column(nullable = false)
    private boolean carryForward = false;

    /** Human-readable policy notes. */
    @Column(length = 500)
    private String description;

   

    public LeavePolicy() {}

    public LeavePolicy(EmployeeType employeeType, LeaveType leaveType,
                       int totalDays, int maxConsecutiveDays,
                       boolean carryForward, String description) {
        this.employeeType      = employeeType;
        this.leaveType         = leaveType;
        this.totalDays         = totalDays;
        this.maxConsecutiveDays = maxConsecutiveDays;
        this.carryForward      = carryForward;
        this.description       = description;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────

    public Long         getId()                  { return id; }
    public EmployeeType getEmployeeType()        { return employeeType; }
    public LeaveType    getLeaveType()           { return leaveType; }
    public int          getTotalDays()           { return totalDays; }
    public int          getMaxConsecutiveDays()  { return maxConsecutiveDays; }
    public boolean      isCarryForward()         { return carryForward; }
    public String       getDescription()        { return description; }

    public void setId(Long id)                              { this.id = id; }
    public void setEmployeeType(EmployeeType employeeType)  { this.employeeType = employeeType; }
    public void setLeaveType(LeaveType leaveType)           { this.leaveType = leaveType; }
    public void setTotalDays(int totalDays)                 { this.totalDays = totalDays; }
    public void setMaxConsecutiveDays(int v)                { this.maxConsecutiveDays = v; }
    public void setCarryForward(boolean carryForward)       { this.carryForward = carryForward; }
    public void setDescription(String description)          { this.description = description; }
}
