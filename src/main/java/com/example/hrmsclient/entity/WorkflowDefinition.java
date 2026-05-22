// ── WorkflowDefinition.java ───────────────────────────────────────────────────
package com.example.hrmsclient.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

/**
 * Template for a workflow (e.g. "Leave Approval", "Expense Approval").
 * Defines trigger, steps, and approval roles.
 */
@Entity
@Table(name = "workflow_definitions")
@EntityListeners(AuditingEntityListener.class)
public class WorkflowDefinition {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;            // e.g. "Leave Approval"

    @Column(nullable = false, length = 50)
    private String triggerEvent;    // LEAVE_REQUEST | EXPENSE | ONBOARDING | OFFBOARDING | CUSTOM

    // JSON array of steps: [{"step":1,"role":"MANAGER","action":"APPROVE"},{"step":2,"role":"HR",...}]
    @Column(columnDefinition = "TEXT")
    private String stepsJson;

    @Column(length = 20)
    private String status = "ACTIVE"; // ACTIVE | INACTIVE

    @Column(length = 300)
    private String description;

    @CreatedDate @Column(updatable = false)
    private LocalDateTime createdAt;

    // Getters & Setters
    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }
    public String getName()                      { return name; }
    public void setName(String v)                { this.name = v; }
    public String getTriggerEvent()              { return triggerEvent; }
    public void setTriggerEvent(String v)        { this.triggerEvent = v; }
    public String getStepsJson()                 { return stepsJson; }
    public void setStepsJson(String v)           { this.stepsJson = v; }
    public String getStatus()                    { return status; }
    public void setStatus(String v)              { this.status = v; }
    public String getDescription()               { return description; }
    public void setDescription(String v)         { this.description = v; }
    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime v)    { this.createdAt = v; }
}