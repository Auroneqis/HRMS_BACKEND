package com.example.hrmsclient.dto;

public class AttendanceSummaryDTO {

    private Long employeeId;
    private int year;
    private int month;
    private long presentDays;
    private long absentDays;
    private long halfDays;
    private long leaveDays;
    private long wfhDays;
    private long totalMarked;

    public AttendanceSummaryDTO(Long employeeId, int year, int month,
                                 long presentDays, long absentDays,
                                 long halfDays, long leaveDays,
                                 long wfhDays, long totalMarked) {
        this.employeeId  = employeeId;
        this.year        = year;
        this.month       = month;
        this.presentDays = presentDays;
        this.absentDays  = absentDays;
        this.halfDays    = halfDays;
        this.leaveDays   = leaveDays;
        this.wfhDays     = wfhDays;
        this.totalMarked = totalMarked;
    }

    public Long getEmployeeId()   { return employeeId;  }
    public int getYear()          { return year;        }
    public int getMonth()         { return month;       }
    public long getPresentDays()  { return presentDays; }
    public long getAbsentDays()   { return absentDays;  }
    public long getHalfDays()     { return halfDays;    }
    public long getLeaveDays()    { return leaveDays;   }
    public long getWfhDays()      { return wfhDays;     }
    public long getTotalMarked()  { return totalMarked; }
}