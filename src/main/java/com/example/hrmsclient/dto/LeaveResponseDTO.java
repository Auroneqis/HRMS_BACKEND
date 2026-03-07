package com.example.hrmsclient.dto;

import com.example.hrmsclient.entity.LeaveStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveResponseDTO {

    private Long id;
    private String employeeId;
    private String employeeName;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private long leaveDays;
    private LeaveStatus status;
    private String reason;
    private String rejectionReason;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    public LeaveResponseDTO() {}
    public LeaveResponseDTO(Long id, String employeeId, String employeeName,
                             String leaveType, LocalDate startDate, LocalDate endDate,
                             long leaveDays, LeaveStatus status, String reason,
                             String rejectionReason, String approvedBy,
                             LocalDateTime approvedAt, LocalDateTime createdAt) {
        this.id              = id;
        this.employeeId      = employeeId;
        this.employeeName    = employeeName;
        this.leaveType       = leaveType;
        this.startDate       = startDate;
        this.endDate         = endDate;
        this.leaveDays       = leaveDays;
        this.status          = status;
        this.reason          = reason;
        this.rejectionReason = rejectionReason;
        this.approvedBy      = approvedBy;
        this.approvedAt      = approvedAt;
        this.createdAt       = createdAt;
    }

    public Long getId()                   { return id;              }
    public String getEmployeeId()         { return employeeId;      }
    public String getEmployeeName()       { return employeeName;    }
    public String getLeaveType()          { return leaveType;       }
    public LocalDate getStartDate()       { return startDate;       }
    public LocalDate getEndDate()         { return endDate;         }
    public long getLeaveDays()            { return leaveDays;       }
    public LeaveStatus getStatus()        { return status;          }
    public String getReason()             { return reason;          }
    public String getRejectionReason()    { return rejectionReason; }
    public String getApprovedBy()         { return approvedBy;      }
    public LocalDateTime getApprovedAt()  { return approvedAt;      }
    public LocalDateTime getCreatedAt()   { return createdAt;       }

    public void setId(Long id)                          { this.id              = id;              }
    public void setEmployeeId(String employeeId)        { this.employeeId      = employeeId;      }
    public void setEmployeeName(String employeeName)    { this.employeeName    = employeeName;    }
    public void setLeaveType(String leaveType)          { this.leaveType       = leaveType;       }
    public void setStartDate(LocalDate startDate)       { this.startDate       = startDate;       }
    public void setEndDate(LocalDate endDate)           { this.endDate         = endDate;         }
    public void setLeaveDays(long leaveDays)            { this.leaveDays       = leaveDays;       }
    public void setStatus(LeaveStatus status)           { this.status          = status;          }
    public void setReason(String reason)                { this.reason          = reason;          }
    public void setRejectionReason(String reason)       { this.rejectionReason = reason;          }
    public void setApprovedBy(String approvedBy)        { this.approvedBy      = approvedBy;      }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt      = approvedAt;      }
    public void setCreatedAt(LocalDateTime createdAt)   { this.createdAt       = createdAt;       }
}