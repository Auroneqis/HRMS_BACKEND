package com.example.hrmsclient.dto;

import com.example.hrmsclient.entity.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceResponseDTO {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private LocalDate attendanceDate;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private AttendanceStatus status;
    private String remarks;
    private String workingHours;
    private LocalDateTime createdAt;
    private AttendanceResponseDTO(Long id, Long employeeId, String employeeName,
                                   String employeeCode, LocalDate attendanceDate,
                                   LocalDateTime checkIn, LocalDateTime checkOut,
                                   AttendanceStatus status, String remarks,
                                   String workingHours, LocalDateTime createdAt) {
        this.id             = id;
        this.employeeId     = employeeId;
        this.employeeName   = employeeName;
        this.employeeCode   = employeeCode;
        this.attendanceDate = attendanceDate;
        this.checkIn        = checkIn;
        this.checkOut       = checkOut;
        this.status         = status;
        this.remarks        = remarks;
        this.workingHours   = workingHours;
        this.createdAt      = createdAt;
    }

    public static AttendanceResponseDTO from(com.example.hrmsclient.entity.Attendance a) {
        String hours = "—";
        if (a.getCheckIn() != null && a.getCheckOut() != null) {
            long mins = java.time.Duration.between(a.getCheckIn(), a.getCheckOut()).toMinutes();
            hours = (mins / 60) + "h " + (mins % 60) + "m";
        }

        return new AttendanceResponseDTO(
            a.getId(),
            a.getEmployee().getId(),
            a.getEmployee().getFullName(),
            a.getEmployee().getEmployeeId(),
            a.getAttendanceDate(),
            a.getCheckIn(),
            a.getCheckOut(),
            a.getStatus(),
            a.getRemarks(),
            hours,
            a.getCreatedAt()
        );
    }
    public Long getId()                  { return id;             }
    public Long getEmployeeId()          { return employeeId;     }
    public String getEmployeeName()      { return employeeName;   }
    public String getEmployeeCode()      { return employeeCode;   }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public LocalDateTime getCheckIn()    { return checkIn;        }
    public LocalDateTime getCheckOut()   { return checkOut;       }
    public AttendanceStatus getStatus()  { return status;         }
    public String getRemarks()           { return remarks;        }
    public String getWorkingHours()      { return workingHours;   }
    public LocalDateTime getCreatedAt()  { return createdAt;      }
}