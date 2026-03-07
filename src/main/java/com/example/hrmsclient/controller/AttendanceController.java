package com.example.hrmsclient.controller;

import com.example.hrmsclient.dto.CheckInRequestDTO;
import com.example.hrmsclient.entity.Attendance;
import com.example.hrmsclient.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/checkin")
    public ResponseEntity<?> checkIn(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CheckInRequestDTO request) {

        Attendance attendance = attendanceService.checkIn(
                userDetails.getUsername(), request);

        return ResponseEntity.ok(Map.of(
            "status",  "success",
            "message", "Check-in successful",
            "data", Map.of(
                "attendanceId", attendance.getId(),
                "checkInTime",  attendance.getCheckIn().toString(),
                "photoUrl",     attendance.getLoginPhotoUrl(),
                "latitude",     attendance.getCheckInLatitude(),
                "longitude",    attendance.getCheckInLongitude(),
                "address",      attendance.getCheckInAddress() != null
                                    ? attendance.getCheckInAddress() : "N/A"
            )
        ));
    }
    @PostMapping("/checkout")
    public ResponseEntity<?> checkOut(
            @AuthenticationPrincipal UserDetails userDetails) {

        Attendance attendance = attendanceService.checkOut(
                userDetails.getUsername());

        long hours   = attendance.getWorkingHours().toHours();
        long minutes = attendance.getWorkingHours().toMinutesPart();

        return ResponseEntity.ok(Map.of(
            "status",  "success",
            "message", "Check-out successful",
            "data", Map.of(
                "attendanceId", attendance.getId(),
                "checkInTime",  attendance.getCheckIn().toString(),
                "checkOutTime", attendance.getCheckOut().toString(),
                "workingHours", hours + "h " + minutes + "m"
            )
        ));
    }
    
    @GetMapping("/today")
    public ResponseEntity<?> getTodayAttendance(
            @AuthenticationPrincipal UserDetails userDetails) {

        Attendance attendance = attendanceService.getTodayAttendance(
                userDetails.getUsername());

        if (attendance == null) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "No attendance for today",
                    "data", null
            ));
        }

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", Map.of(
                        "attendanceId", attendance.getId(),
                        "checkInTime", attendance.getCheckIn(),
                        "checkOutTime", attendance.getCheckOut(),
                        "workingHours", attendance.getWorkingHours(),
                        "latitude", attendance.getCheckInLatitude(),
                        "longitude", attendance.getCheckInLongitude()
                )
        ));
    }
}