package com.example.hrmsclient.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

/**
 * A running instance of a workflow triggered by an employee action.
 */
@Entity
@Table(name = "workflow_instances",
    indexes = {
        @Index(name = "idx_wf_emp",     columnList = "employee_id"),
        @Index(name = "idx_wf_status",  columnList = "status"),
        @Index(name = "idx_wf_trigger", columnList = "triggerEvent")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class WorkflowInstance {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "definition_id")
    private WorkflowDefinition definition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false, length = 50)
    private String triggerEvent;    // mirrors definition.triggerEvent

    // ID of the source record (leave request ID, expense ID, etc.)
    @Column(length = 100)
    private String referenceId;

    // PENDING | IN_PROGRESS | APPROVED | REJECTED | CANCELLED
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    private int currentStep = 1;    // which step is awaiting action

    // Stores full approval history as JSON
    @Column(columnDefinition = "TEXT")
    private String actionsJson;

    @Column(length = 300)
    private String remarks;

    @Column(length = 100)
    private String currentApprover;  // username of who needs to act next

    @CreatedDate    @Column(updatable = false) private LocalDateTime createdAt;
    @LastModifiedDate                           private LocalDateTime updatedAt;

    // Getters & Setters
    public Long getId()                              { return id; }
    public void setId(Long id)                       { this.id = id; }
    public WorkflowDefinition getDefinition()        { return definition; }
    public void setDefinition(WorkflowDefinition v)  { this.definition = v; }
    public Employee getEmployee()                    { return employee; }
    public void setEmployee(Employee v)              { this.employee = v; }
    public String getTriggerEvent()                  { return triggerEvent; }
    public void setTriggerEvent(String v)            { this.triggerEvent = v; }
    public String getReferenceId()                   { return referenceId; }
    public void setReferenceId(String v)             { this.referenceId = v; }
    public String getStatus()                        { return status; }
    public void setStatus(String v)                  { this.status = v; }
    public int getCurrentStep()                      { return currentStep; }
    public void setCurrentStep(int v)                { this.currentStep = v; }
    public String getActionsJson()                   { return actionsJson; }
    public void setActionsJson(String v)             { this.actionsJson = v; }
    public String getRemarks()                       { return remarks; }
    public void setRemarks(String v)                 { this.remarks = v; }
    public String getCurrentApprover()               { return currentApprover; }
    public void setCurrentApprover(String v)         { this.currentApprover = v; }
    public LocalDateTime getCreatedAt()              { return createdAt; }
    public void setCreatedAt(LocalDateTime v)        { this.createdAt = v; }
    public LocalDateTime getUpdatedAt()              { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)        { this.updatedAt = v; }
}