package com.example.hrmsclient.dto;

import java.time.LocalDateTime;

public class AdminResponseDTO {

    private Long          id;
    private String        adminId;
    private String        fullName;
    private String        firstName;
    private String        lastName;
    private String        emailId;
    private String        phone;
    private String        profilePhotoUrl;
    private String        role;
    private String        department;
    private String        designation;
    /** Employee ID of assigned reporting manager, if any */
    private String        reportingManager;
    private boolean       active;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId()                              { return id; }
    public void setId(Long v)                        { this.id = v; }
    public String getAdminId()                       { return adminId; }
    public void setAdminId(String v)                 { this.adminId = v; }
    public String getFullName()                      { return fullName; }
    public void setFullName(String v)                { this.fullName = v; }
    public String getFirstName()                     { return firstName; }
    public void setFirstName(String v)               { this.firstName = v; }
    public String getLastName()                      { return lastName; }
    public void setLastName(String v)                { this.lastName = v; }
    public String getEmailId()                       { return emailId; }
    public void setEmailId(String v)                 { this.emailId = v; }
    public String getPhone()                         { return phone; }
    public void setPhone(String v)                   { this.phone = v; }
    public String getProfilePhotoUrl()               { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String v)         { this.profilePhotoUrl = v; }
    public String getRole()                          { return role; }
    public void setRole(String v)                    { this.role = v; }
    public String getDepartment()                    { return department; }
    public void setDepartment(String v)              { this.department = v; }
    public String getDesignation()                   { return designation; }
    public void setDesignation(String v)             { this.designation = v; }
    public String getReportingManager()            { return reportingManager; }
    public void setReportingManager(String v)      { this.reportingManager = v; }
    public boolean isActive()                        { return active; }
    public void setActive(boolean v)                 { this.active = v; }
    public LocalDateTime getLastLoginAt()            { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime v)      { this.lastLoginAt = v; }
    public LocalDateTime getCreatedAt()              { return createdAt; }
    public void setCreatedAt(LocalDateTime v)        { this.createdAt = v; }
    public LocalDateTime getUpdatedAt()              { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)        { this.updatedAt = v; }
}