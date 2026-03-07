package com.example.hrmsclient.entity;

public enum PayrollStatus {
    DRAFT,       // Payroll calculated but not reviewed
    PENDING,     // Waiting for HR/Admin approval
    APPROVED,    // Approved — ready for payment
    PROCESSING,  // Bank transfer in progress
    PAID,        // Amount deposited to account 
    FAILED,      // Bank transfer failed
    CANCELLED,   // Payroll cancelled
    ON_HOLD      // Payroll on hold
}