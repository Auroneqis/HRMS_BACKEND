package com.example.hrmsclient.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceRequestDTO {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private String status;
    private String remarks;

    public AttendanceRequestDTO() {}

    public AttendanceRequestDTO(Long employeeId, LocalDate attendanceDate,
                                LocalDateTime checkIn, LocalDateTime checkOut,
                                String status, String remarks) {
        this.employeeId      = employeeId;
        this.attendanceDate  = attendanceDate;
        this.checkIn         = checkIn;
        this.checkOut        = checkOut;
        this.status          = status;
        this.remarks         = remarks;
    }
    public Long getEmployeeId()             { return employeeId;     }
    public LocalDate getAttendanceDate()    { return attendanceDate; }
    public LocalDateTime getCheckIn()       { return checkIn;        }
    public LocalDateTime getCheckOut()      { return checkOut;       }
    public String getStatus()               { return status;         }
    public String getRemarks()              { return remarks;        }

    public void setEmployeeId(Long employeeId)              { this.employeeId     = employeeId;     }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    public void setCheckIn(LocalDateTime checkIn)           { this.checkIn        = checkIn;        }
    public void setCheckOut(LocalDateTime checkOut)         { this.checkOut       = checkOut;       }
    public void setStatus(String status)                    { this.status         = status;         }
    public void setRemarks(String remarks)                  { this.remarks        = remarks;        }
}