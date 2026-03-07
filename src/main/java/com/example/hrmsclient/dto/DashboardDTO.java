package com.example.hrmsclient.dto;

public class DashboardDTO {

    private long totalEmployees;
    private long activeEmployees;
    private long onNoticePeriod;
    private long terminated;

    public DashboardDTO() {}

    public DashboardDTO(long totalEmployees, long activeEmployees,
                        long onNoticePeriod, long terminated) {
        this.totalEmployees  = totalEmployees;
        this.activeEmployees = activeEmployees;
        this.onNoticePeriod  = onNoticePeriod;
        this.terminated      = terminated;
    }

    public long getTotalEmployees()   { return totalEmployees;  }
    public long getActiveEmployees()  { return activeEmployees; }
    public long getOnNoticePeriod()   { return onNoticePeriod;  }
    public long getTerminated()       { return terminated;      }

    public void setTotalEmployees(long totalEmployees)   { this.totalEmployees  = totalEmployees;  }
    public void setActiveEmployees(long activeEmployees) { this.activeEmployees = activeEmployees; }
    public void setOnNoticePeriod(long onNoticePeriod)   { this.onNoticePeriod  = onNoticePeriod;  }
    public void setTerminated(long terminated)           { this.terminated      = terminated;      }
}