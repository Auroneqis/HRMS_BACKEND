package com.example.hrmsclient.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminRequestDTO {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String emailId;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String phone;
    private String profilePhotoUrl;
    private String role;           // ADMIN or SUPER_ADMIN
    private String department;
    private String designation;
    /** Optional Employee ID of reporting manager (MANAGER / HR / ADMIN employee) */
    private String reportingManager;

    public String getFirstName()             { return firstName; }
    public void setFirstName(String v)       { this.firstName = v; }
    public String getLastName()              { return lastName; }
    public void setLastName(String v)        { this.lastName = v; }
    public String getEmailId()               { return emailId; }
    public void setEmailId(String v)         { this.emailId = v; }
    public String getPassword()              { return password; }
    public void setPassword(String v)        { this.password = v; }
    public String getPhone()                 { return phone; }
    public void setPhone(String v)           { this.phone = v; }
    public String getProfilePhotoUrl()       { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String v) { this.profilePhotoUrl = v; }
    public String getRole()                  { return role; }
    public void setRole(String v)            { this.role = v; }
    public String getDepartment()            { return department; }
    public void setDepartment(String v)      { this.department = v; }
    public String getDesignation()           { return designation; }
    public void setDesignation(String v)     { this.designation = v; }
    public String getReportingManager()      { return reportingManager; }
    public void setReportingManager(String v) { this.reportingManager = v; }
}