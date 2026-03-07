package com.example.hrmsclient.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class LeaveRequestDTO {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotBlank(message = "Leave type is required")
    private String leaveType;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @Size(max = 500)
    private String reason;

    // ✅ No-arg constructor
    public LeaveRequestDTO() {}

    // ✅ All-arg constructor
    public LeaveRequestDTO(Long employeeId, String leaveType,
                           LocalDate startDate, LocalDate endDate,
                           String reason) {
        this.employeeId = employeeId;
        this.leaveType  = leaveType;
        this.startDate  = startDate;
        this.endDate    = endDate;
        this.reason     = reason;
    }

    public Long getEmployeeId()    { return employeeId; }
    public String getLeaveType()   { return leaveType;  }
    public LocalDate getStartDate(){ return startDate;  }
    public LocalDate getEndDate()  { return endDate;    }
    public String getReason()      { return reason;     }

    public void setEmployeeId(Long employeeId)      { this.employeeId = employeeId; }
    public void setLeaveType(String leaveType)      { this.leaveType  = leaveType;  }
    public void setStartDate(LocalDate startDate)   { this.startDate  = startDate;  }
    public void setEndDate(LocalDate endDate)       { this.endDate    = endDate;    }
    public void setReason(String reason)            { this.reason     = reason;     }
}