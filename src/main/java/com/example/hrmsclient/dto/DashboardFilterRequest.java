package com.example.hrmsclient.dto;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public class DashboardFilterRequest {

    // Employee filters
    private String  employeeId;
    private String  firstName;
    private String  lastName;
    private String  email;
    private String  department;
    private String  designation;
    private String  role;
    private String  employmentStatus;   // ACTIVE, INACTIVE, ON_NOTICE, EXITED
    private String  search;             // global search

    // Attendance filters
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate attendanceDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate attendanceDateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate attendanceDateTo;

    private String  attendanceStatus;   // PRESENT, ABSENT, ON_LEAVE, HALF_DAY, WFH
    private Boolean checkedIn;

    // Payroll filters
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate payrollMonth;

    private String  payrollStatus;      // DRAFT, PENDING, APPROVED, PAID, FAILED

    // Pagination
    private int    page    = 0;
    private int    size    = 10;
    private String sortBy  = "createdAt";
    private String sortDir = "desc";
	public String getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getDesignation() {
		return designation;
	}
	public void setDesignation(String designation) {
		this.designation = designation;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getEmploymentStatus() {
		return employmentStatus;
	}
	public void setEmploymentStatus(String employmentStatus) {
		this.employmentStatus = employmentStatus;
	}
	public String getSearch() {
		return search;
	}
	public void setSearch(String search) {
		this.search = search;
	}
	public LocalDate getAttendanceDate() {
		return attendanceDate;
	}
	public void setAttendanceDate(LocalDate attendanceDate) {
		this.attendanceDate = attendanceDate;
	}
	public LocalDate getAttendanceDateFrom() {
		return attendanceDateFrom;
	}
	public void setAttendanceDateFrom(LocalDate attendanceDateFrom) {
		this.attendanceDateFrom = attendanceDateFrom;
	}
	public LocalDate getAttendanceDateTo() {
		return attendanceDateTo;
	}
	public void setAttendanceDateTo(LocalDate attendanceDateTo) {
		this.attendanceDateTo = attendanceDateTo;
	}
	public String getAttendanceStatus() {
		return attendanceStatus;
	}
	public void setAttendanceStatus(String attendanceStatus) {
		this.attendanceStatus = attendanceStatus;
	}
	public Boolean getCheckedIn() {
		return checkedIn;
	}
	public void setCheckedIn(Boolean checkedIn) {
		this.checkedIn = checkedIn;
	}
	public LocalDate getPayrollMonth() {
		return payrollMonth;
	}
	public void setPayrollMonth(LocalDate payrollMonth) {
		this.payrollMonth = payrollMonth;
	}
	public String getPayrollStatus() {
		return payrollStatus;
	}
	public void setPayrollStatus(String payrollStatus) {
		this.payrollStatus = payrollStatus;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public String getSortBy() {
		return sortBy;
	}
	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}
	public String getSortDir() {
		return sortDir;
	}
	public void setSortDir(String sortDir) {
		this.sortDir = sortDir;
	}

    
}