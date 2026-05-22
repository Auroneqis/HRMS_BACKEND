package com.example.hrmsclient.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "applicants",
    indexes = {
        @Index(name = "idx_app_job",    columnList = "job_posting_id"),
        @Index(name = "idx_app_email",  columnList = "email"),
        @Index(name = "idx_app_stage",  columnList = "stage")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class Applicant {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 15)
    private String phone;

    @Column(length = 500)
    private String resumeUrl;

    @Column(length = 500)
    private String linkedInUrl;

    @Column(length = 50)
    private String currentCompany;

    @Column(length = 50)
    private String totalExperience;

    @Column(length = 50)
    private String currentCtc;

    @Column(length = 50)
    private String expectedCtc;

    @Column(length = 30)
    private String noticePeriod;

    /**
     * ATS pipeline stage:
     * APPLIED → SCREENING → INTERVIEW → TECHNICAL → HR_ROUND → OFFER → HIRED | REJECTED
     */
    @Column(nullable = false, length = 30)
    private String stage = "APPLIED";

    // ACTIVE | REJECTED | WITHDRAWN | HIRED
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(length = 100)
    private String assignedTo;   // HR/interviewer username

    // When hired, link to the created Employee
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private LocalDate expectedJoiningDate;

    @CreatedDate    @Column(updatable = false) private LocalDateTime appliedAt;
    @LastModifiedDate                           private LocalDateTime updatedAt;

    // Getters & Setters
    public Long getId()                           { return id; }
    public void setId(Long id)                    { this.id = id; }
    public JobPosting getJobPosting()             { return jobPosting; }
    public void setJobPosting(JobPosting v)       { this.jobPosting = v; }
    public String getFullName()                   { return fullName; }
    public void setFullName(String v)             { this.fullName = v; }
    public String getEmail()                      { return email; }
    public void setEmail(String v)                { this.email = v; }
    public String getPhone()                      { return phone; }
    public void setPhone(String v)                { this.phone = v; }
    public String getResumeUrl()                  { return resumeUrl; }
    public void setResumeUrl(String v)            { this.resumeUrl = v; }
    public String getLinkedInUrl()                { return linkedInUrl; }
    public void setLinkedInUrl(String v)          { this.linkedInUrl = v; }
    public String getCurrentCompany()             { return currentCompany; }
    public void setCurrentCompany(String v)       { this.currentCompany = v; }
    public String getTotalExperience()            { return totalExperience; }
    public void setTotalExperience(String v)      { this.totalExperience = v; }
    public String getCurrentCtc()                 { return currentCtc; }
    public void setCurrentCtc(String v)           { this.currentCtc = v; }
    public String getExpectedCtc()                { return expectedCtc; }
    public void setExpectedCtc(String v)          { this.expectedCtc = v; }
    public String getNoticePeriod()               { return noticePeriod; }
    public void setNoticePeriod(String v)         { this.noticePeriod = v; }
    public String getStage()                      { return stage; }
    public void setStage(String v)                { this.stage = v; }
    public String getStatus()                     { return status; }
    public void setStatus(String v)               { this.status = v; }
    public String getNotes()                      { return notes; }
    public void setNotes(String v)                { this.notes = v; }
    public String getAssignedTo()                 { return assignedTo; }
    public void setAssignedTo(String v)           { this.assignedTo = v; }
    public Employee getEmployee()                 { return employee; }
    public void setEmployee(Employee v)           { this.employee = v; }
    public LocalDate getExpectedJoiningDate()     { return expectedJoiningDate; }
    public void setExpectedJoiningDate(LocalDate v){ this.expectedJoiningDate = v; }
    public LocalDateTime getAppliedAt()           { return appliedAt; }
    public void setAppliedAt(LocalDateTime v)     { this.appliedAt = v; }
    public LocalDateTime getUpdatedAt()           { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)     { this.updatedAt = v; }
}