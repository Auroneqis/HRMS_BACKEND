package com.example.hrmsclient.entity;


public enum LeaveType {
    PLANNED("Planned", true, false, false),
 
    SICK_LEGACY("Sick", true, false, false),
    /** Casual Leave – personal work, urgent tasks, short planned breaks */
    CASUAL("Casual Leave", true, false, false),

    /** Sick Leave – employee is unwell or medical reasons */
    SICK("Sick Leave", true, false, false),

    /**
     * Earned / Paid Leave – accumulated paid leave for vacations.
     * Only 3 consecutive days allowed as EL; rest treated as LOP.
     */
    EARNED("Earned Leave", true, false, false),

    /** Work From Home – remote work (optional, if company allows) */
    WFH("Work From Home", true, false, false),

    /** Maternity Leave – for female employees as per policy/law */
    MATERNITY("Maternity Leave", true, true, false),

    /** Bereavement Leave – death in immediate family */
    BEREAVEMENT("Bereavement Leave", true, false, false),

    /** Marriage Leave – optional benefit for employee's own wedding */
    MARRIAGE("Marriage Leave", true, false, false),

    /** Loss of Pay / Unpaid Leave – when paid leaves are exhausted */
    LOP("Loss of Pay", false, false, false),

    /** Public Holidays – company-declared holidays */
    PUBLIC_HOLIDAY("Public Holiday", true, false, true),

    /** Optional / Festival Holiday – employee chooses based on religion/culture */
    OPTIONAL_HOLIDAY("Optional Holiday", true, false, true),

    /** Paternity Leave – for male employees (25 days) */
    PATERNITY("Paternity Leave", true, false, false);

    // ── Metadata fields ────────────────────────────────────────────────────
    private final String displayName;
    /** Is this leave type paid? */
    private final boolean paid;
    /** Requires gender-specific eligibility check? */
    private final boolean genderRestricted;
    /** Is this a company-declared holiday (not employee-initiated)? */
    private final boolean companyHoliday;

    LeaveType(String displayName, boolean paid,
              boolean genderRestricted, boolean companyHoliday) {
        this.displayName      = displayName;
        this.paid             = paid;
        this.genderRestricted = genderRestricted;
        this.companyHoliday   = companyHoliday;
    }

    public String  getDisplayName()      { return displayName;      }
    public boolean isPaid()              { return paid;              }
    public boolean isGenderRestricted()  { return genderRestricted;  }
    public boolean isCompanyHoliday()    { return companyHoliday;    }

    /** Resolve a DB string (name or displayName) back to the enum constant. */
    public static LeaveType fromString(String value) {
        if (value == null) return null;
        for (LeaveType lt : values()) {
            if (lt.name().equalsIgnoreCase(value)
                    || lt.displayName.equalsIgnoreCase(value)) {
                return lt;
            }
        }
        throw new IllegalArgumentException("Unknown leave type: " + value);
    }
}
