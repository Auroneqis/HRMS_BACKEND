package com.example.hrmsclient.service;

import com.example.hrmsclient.entity.Employee;
import com.example.hrmsclient.entity.Payroll;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * PayrollCalculationService
 * ─────────────────────────
 * Pure calculation engine — no DB access.
 * Follows Indian payroll standards:
 *   - PF     : 12% of Basic (employee) + 12% (employer)
 *   - ESI    : 0.75% of Gross (employee) + 3.25% (employer) — only if gross <= 21,000
 *   - PT     : Professional Tax (state-wise slab, max ₹200/month)
 *   - TDS    : Simplified monthly TDS estimate
 *   - HRA    : 40% of Basic (non-metro) / 50% (metro)
 *   - DA     : 10% of Basic
 */
@Component
public class PayrollCalculationService {

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final BigDecimal PF_RATE           = new BigDecimal("0.12");   // 12%
    private static final BigDecimal ESI_EMP_RATE      = new BigDecimal("0.0075"); // 0.75%
    private static final BigDecimal ESI_EMPLOYER_RATE = new BigDecimal("0.0325"); // 3.25%
    private static final BigDecimal HRA_RATE          = new BigDecimal("0.40");   // 40% non-metro
    private static final BigDecimal DA_RATE           = new BigDecimal("0.10");   // 10%
    private static final BigDecimal ESI_GROSS_LIMIT   = new BigDecimal("21000");  // ESI applies if gross <= 21000
    private static final BigDecimal PT_AMOUNT         = new BigDecimal("200");    // Professional Tax

    public Payroll calculate(Employee employee, Payroll payroll) {

        // ── Step 1: Per-Day Salary ────────────────────────────────────────────
        BigDecimal monthlyCTC   = BigDecimal.valueOf(employee.getBasicEmployeeSalary());
        int workingDays         = payroll.getWorkingDays();
        int presentDays         = payroll.getPresentDays();

        // If employee was absent, deduct proportionally (LOP — Loss of Pay)
        BigDecimal perDaySalary = workingDays > 0
                ? monthlyCTC.divide(BigDecimal.valueOf(workingDays), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal effectiveSalary = perDaySalary.multiply(BigDecimal.valueOf(presentDays))
                .setScale(2, RoundingMode.HALF_UP);

        // ── Step 2: Salary Breakup ────────────────────────────────────────────
        BigDecimal basic            = effectiveSalary.multiply(new BigDecimal("0.50"))
                                        .setScale(2, RoundingMode.HALF_UP);   // 50% of CTC
        BigDecimal hra              = basic.multiply(HRA_RATE)
                                        .setScale(2, RoundingMode.HALF_UP);   // 40% of basic
        BigDecimal da               = basic.multiply(DA_RATE)
                                        .setScale(2, RoundingMode.HALF_UP);   // 10% of basic
        BigDecimal overtime         = payroll.getOvertimeAmount() != null
                                        ? payroll.getOvertimeAmount() : BigDecimal.ZERO;
        BigDecimal bonus            = payroll.getBonusAmount() != null
                                        ? payroll.getBonusAmount() : BigDecimal.ZERO;
        BigDecimal reimbursement    = payroll.getReimbursement() != null
                                        ? payroll.getReimbursement() : BigDecimal.ZERO;
        BigDecimal specialAllowance = effectiveSalary
                                        .subtract(basic).subtract(hra).subtract(da)
                                        .setScale(2, RoundingMode.HALF_UP);   // Remaining

        BigDecimal grossSalary      = basic.add(hra).add(da)
                                        .add(specialAllowance)
                                        .add(overtime).add(bonus).add(reimbursement)
                                        .setScale(2, RoundingMode.HALF_UP);

        // ── Step 3: Deductions ────────────────────────────────────────────────

        // PF — 12% of basic (capped at ₹15,000 basic for PF calculation)
        BigDecimal pfBase       = basic.min(new BigDecimal("15000"));
        BigDecimal pfEmployee   = pfBase.multiply(PF_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pfEmployer   = pfBase.multiply(PF_RATE).setScale(2, RoundingMode.HALF_UP);

        // ESI — only if gross <= ₹21,000
        BigDecimal esiEmployee  = BigDecimal.ZERO;
        BigDecimal esiEmployer  = BigDecimal.ZERO;
        if (grossSalary.compareTo(ESI_GROSS_LIMIT) <= 0) {
            esiEmployee = grossSalary.multiply(ESI_EMP_RATE).setScale(2, RoundingMode.HALF_UP);
            esiEmployer = grossSalary.multiply(ESI_EMPLOYER_RATE).setScale(2, RoundingMode.HALF_UP);
        }

        // Professional Tax — state slab (simplified: ₹200 if gross > ₹15,000)
        BigDecimal professionalTax = grossSalary.compareTo(new BigDecimal("15000")) > 0
                ? PT_AMOUNT : BigDecimal.ZERO;

        // TDS — simplified monthly estimate (annual tax / 12)
        BigDecimal tds = calculateTDS(grossSalary);

        // Loan deduction
        BigDecimal loanDeduction  = payroll.getLoanDeduction() != null
                ? payroll.getLoanDeduction() : BigDecimal.ZERO;
        BigDecimal otherDeduction = payroll.getOtherDeduction() != null
                ? payroll.getOtherDeduction() : BigDecimal.ZERO;

        BigDecimal totalDeductions = pfEmployee.add(esiEmployee)
                .add(professionalTax).add(tds)
                .add(loanDeduction).add(otherDeduction)
                .setScale(2, RoundingMode.HALF_UP);

        // ── Step 4: Net Salary = Gross - Deductions ───────────────────────────
        BigDecimal netSalary = grossSalary.subtract(totalDeductions)
                .setScale(2, RoundingMode.HALF_UP);

        // ── Step 5: Set all values on Payroll ─────────────────────────────────
        payroll.setBasicSalary(basic);
        payroll.setHra(hra);
        payroll.setDa(da);
        payroll.setSpecialAllowance(specialAllowance);
        payroll.setOvertimeAmount(overtime);
        payroll.setBonusAmount(bonus);
        payroll.setReimbursement(reimbursement);
        payroll.setGrossSalary(grossSalary);

        payroll.setPfEmployee(pfEmployee);
        payroll.setPfEmployer(pfEmployer);
        payroll.setEsiEmployee(esiEmployee);
        payroll.setEsiEmployer(esiEmployer);
        payroll.setProfessionalTax(professionalTax);
        payroll.setTds(tds);
        payroll.setLoanDeduction(loanDeduction);
        payroll.setOtherDeduction(otherDeduction);
        payroll.setTotalDeductions(totalDeductions);
        payroll.setNetSalary(netSalary);

        return payroll;
    }

    // ── TDS Calculation (Simplified) ─────────────────────────────────────────
    // Annual Income Tax Slabs (New Regime FY 2024-25):
    // 0 - 3L       : Nil
    // 3L - 7L      : 5%
    // 7L - 10L     : 10%
    // 10L - 12L    : 15%
    // 12L - 15L    : 20%
    // Above 15L    : 30%
    private BigDecimal calculateTDS(BigDecimal monthlyGross) {
        BigDecimal annualGross  = monthlyGross.multiply(new BigDecimal("12"));
        BigDecimal annualTax    = BigDecimal.ZERO;

        BigDecimal l3  = new BigDecimal("300000");
        BigDecimal l7  = new BigDecimal("700000");
        BigDecimal l10 = new BigDecimal("1000000");
        BigDecimal l12 = new BigDecimal("1200000");
        BigDecimal l15 = new BigDecimal("1500000");

        if (annualGross.compareTo(l3) <= 0) {
            annualTax = BigDecimal.ZERO;
        } else if (annualGross.compareTo(l7) <= 0) {
            annualTax = annualGross.subtract(l3).multiply(new BigDecimal("0.05"));
        } else if (annualGross.compareTo(l10) <= 0) {
            annualTax = new BigDecimal("20000")
                    .add(annualGross.subtract(l7).multiply(new BigDecimal("0.10")));
        } else if (annualGross.compareTo(l12) <= 0) {
            annualTax = new BigDecimal("50000")
                    .add(annualGross.subtract(l10).multiply(new BigDecimal("0.15")));
        } else if (annualGross.compareTo(l15) <= 0) {
            annualTax = new BigDecimal("80000")
                    .add(annualGross.subtract(l12).multiply(new BigDecimal("0.20")));
        } else {
            annualTax = new BigDecimal("140000")
                    .add(annualGross.subtract(l15).multiply(new BigDecimal("0.30")));
        }

        // Add 4% Health & Education Cess
        annualTax = annualTax.multiply(new BigDecimal("1.04"));

        // Monthly TDS = Annual Tax / 12
        return annualTax.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
    }
}