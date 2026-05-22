package com.example.hrmsclient.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_postings",
    indexes = {
        @Index(name = "idx_job_status",      columnList = "status"),
        @Index(name = "idx_job_department",  columnList = "department")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class JobPosting {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(nullable = false, length = 50)
    private String location;

    @Column(length = 30)
    private String employmentType;  // FULL_TIME | PART_TIME | CONTRACT | INTERN

    @Column(length = 30)
    private String experienceLevel; // FRESHER | JUNIOR | MID | SENIOR | LEAD

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(length = 50)
    private String salaryRange;     // e.g. "5-8 LPA"

    private int openPositions = 1;

    // DRAFT | OPEN | PAUSED | CLOSED
    @Column(nullable = false, length = 20)
    private String status = "DRAFT";

    private LocalDate closingDate;

    @Column(length = 100)
    private String createdBy;

    @CreatedDate    @Column(updatable = false) private LocalDateTime createdAt;
    @LastModifiedDate                           private LocalDateTime updatedAt;

    // Getters & Setters
    public Long getId()                         { return id; }
    public void setId(Long id)                  { this.id = id; }
    public String getTitle()                    { return title; }
    public void setTitle(String v)              { this.title = v; }
    public String getDepartment()               { return department; }
    public void setDepartment(String v)         { this.department = v; }
    public String getLocation()                 { return location; }
    public void setLocation(String v)           { this.location = v; }
    public String getEmploymentType()           { return employmentType; }
    public void setEmploymentType(String v)     { this.employmentType = v; }
    public String getExperienceLevel()          { return experienceLevel; }
    public void setExperienceLevel(String v)    { this.experienceLevel = v; }
    public String getDescription()              { return description; }
    public void setDescription(String v)        { this.description = v; }
    public String getRequirements()             { return requirements; }
    public void setRequirements(String v)       { this.requirements = v; }
    public String getSalaryRange()              { return salaryRange; }
    public void setSalaryRange(String v)        { this.salaryRange = v; }
    public int getOpenPositions()               { return openPositions; }
    public void setOpenPositions(int v)         { this.openPositions = v; }
    public String getStatus()                   { return status; }
    public void setStatus(String v)             { this.status = v; }
    public LocalDate getClosingDate()           { return closingDate; }
    public void setClosingDate(LocalDate v)     { this.closingDate = v; }
    public String getCreatedBy()                { return createdBy; }
    public void setCreatedBy(String v)          { this.createdBy = v; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime v)   { this.createdAt = v; }
    public LocalDateTime getUpdatedAt()         { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)   { this.updatedAt = v; }
}