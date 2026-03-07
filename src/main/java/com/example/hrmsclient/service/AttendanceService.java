package com.example.hrmsclient.service;

import com.example.hrmsclient.dto.CheckInRequestDTO;
import com.example.hrmsclient.entity.Attendance;
import com.example.hrmsclient.entity.Employee;
import com.example.hrmsclient.repository.AttendanceRepository;
import com.example.hrmsclient.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository   employeeRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository   = employeeRepository;
    }

    // ── CHECK-IN ──────────────────────────────────────────────────────────────
    public Attendance checkIn(String emailId, CheckInRequestDTO request) {

        // 1. Validate photo URL is provided
        if (request.getLoginPhotoUrl() == null || request.getLoginPhotoUrl().isBlank()) {
            throw new IllegalStateException("Login photo URL is required to check in.");
        }

        // 2. Validate location
        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new IllegalStateException("Location (latitude & longitude) is required to check in.");
        }

        // 3. Load employee
        Employee employee = employeeRepository.findByEmailIdAndDeletedFalse(emailId)
                .orElseThrow(() -> new IllegalStateException("Employee not found."));

        // 4. Prevent duplicate check-in for today
        LocalDate today = LocalDate.now();
        if (attendanceRepository.existsByEmployeeIdAndAttendanceDate(employee.getId(), today)) {
            throw new IllegalStateException("You have already checked in today.");
        }

        // 5. Save attendance record with photo URL + location
        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setAttendanceDate(today);
        attendance.setCheckIn(LocalDateTime.now());
        attendance.setLoginPhotoUrl(request.getLoginPhotoUrl());  
        attendance.setCheckInLatitude(request.getLatitude());
        attendance.setCheckInLongitude(request.getLongitude());
        attendance.setCheckInAddress(request.getAddress());
        attendance.setRemarks(request.getRemarks());

        return attendanceRepository.save(attendance);
    }

    // ── CHECK-OUT ─────────────────────────────────────────────────────────────
    public Attendance checkOut(String emailId) {

        Employee employee = employeeRepository.findByEmailIdAndDeletedFalse(emailId)
                .orElseThrow(() -> new RuntimeException("Employee not found."));

        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndAttendanceDate(employee.getId(), LocalDate.now())
                .orElseThrow(() -> new RuntimeException("No check-in found for today."));

        if (attendance.getCheckOut() != null) {
            throw new IllegalStateException("You have already checked out today.");
        }

        attendance.setCheckOut(LocalDateTime.now());

        // calculate working duration
        Duration duration = Duration.between(attendance.getCheckIn(), attendance.getCheckOut());

        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        attendance.setWorkingHours(hours + "h " + minutes + "m");   // if column exists

        return attendanceRepository.save(attendance);
    }
    public Attendance getTodayAttendance(String email) {

        Employee employee = employeeRepository
                .findByEmailIdAndDeletedFalse(email)
                .orElseThrow(() -> new IllegalStateException("Employee not found"));

        return attendanceRepository
                .findByEmployeeIdAndAttendanceDate(employee.getId(), LocalDate.now())
                .orElse(null);
    }
    
}