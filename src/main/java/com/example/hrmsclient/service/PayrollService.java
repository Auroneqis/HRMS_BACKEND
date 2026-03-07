package com.example.hrmsclient.service;

import com.example.hrmsclient.entity.*;
import com.example.hrmsclient.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PayrollService {

    private static final Logger log = LoggerFactory.getLogger(PayrollService.class);

    private final PayrollRepository         payrollRepository;
    private final EmployeeRepository        employeeRepository;
    private final AttendanceRepository      attendanceRepository;
    private final PayrollCalculationService calculationService;
    private final EmailService              emailService;

    public PayrollService(PayrollRepository payrollRepository,
                          EmployeeRepository employeeRepository,
                          AttendanceRepository attendanceRepository,
                          PayrollCalculationService calculationService,
                          EmailService emailService) {
        this.payrollRepository    = payrollRepository;
        this.employeeRepository   = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.calculationService   = calculationService;
        this.emailService         = emailService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STEP 1 — AUTO GENERATE: 25th of every month at 9 AM
    // HR reviews payroll from 25th–27th before approval
    // ─────────────────────────────────────────────────────────────────────────
    @Scheduled(cron = "0 0 9 25 * ?")
    @Transactional
    public void autoGenerateMonthlyPayroll() {
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        log.info("=== AUTO PAYROLL GENERATION STARTED for {} ===", currentMonth);
        generatePayrollForAllEmployees(currentMonth);
        log.info("=== AUTO PAYROLL GENERATION COMPLETED ===");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STEP 2 — AUTO APPROVE: 27th of every month at 10 AM
    // Auto-approves any still-PENDING payrolls after HR review window
    // ─────────────────────────────────────────────────────────────────────────
    @Scheduled(cron = "0 0 10 27 * ?")
    @Transactional
    public void autoApprovePayroll() {
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        log.info("=== AUTO PAYROLL APPROVAL STARTED for {} ===", currentMonth);

        List<Payroll> pendingPayrolls = payrollRepository
                .findByPayrollMonthAndStatus(currentMonth, PayrollStatus.PENDING);

        for (Payroll payroll : pendingPayrolls) {
            payroll.setStatus(PayrollStatus.APPROVED);
            payroll.setApprovedBy("SYSTEM_AUTO");
            payrollRepository.save(payroll);
        }

        log.info("Auto-approved {} payrolls", pendingPayrolls.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STEP 3 — AUTO PAY: Last day of every month at 11 AM
    // Processes bank transfer for all APPROVED payrolls
    // ─────────────────────────────────────────────────────────────────────────
    @Scheduled(cron = "0 0 11 L * ?")
    @Transactional
    public void autoProcessSalaryPayment() {
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        log.info("=== AUTO SALARY PAYMENT STARTED for {} ===", currentMonth);

        List<Payroll> approvedPayrolls = payrollRepository
                .findByPayrollMonthAndStatus(currentMonth, PayrollStatus.APPROVED);

        int success = 0, failed = 0;

        for (Payroll payroll : approvedPayrolls) {
            try {
                processSinglePayment(payroll);
                success++;
            } catch (Exception e) {
                log.error("Payment failed for employee {}: {}",
                        payroll.getEmployee().getEmployeeId(), e.getMessage());
                payroll.setStatus(PayrollStatus.FAILED);
                payroll.setRemarks("Payment failed: " + e.getMessage());
                payrollRepository.save(payroll);
                failed++;
            }
        }

        log.info("=== PAYMENT COMPLETED: Success={}, Failed={} ===", success, failed);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CORE: Process single employee payment
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public void processSinglePayment(Payroll payroll) {

        // Mark as PROCESSING
        payroll.setStatus(PayrollStatus.PROCESSING);
        payrollRepository.save(payroll);

        // ── Initiate Bank Transfer ────────────────────────────────────────────
        // In production: replace with RazorpayX / Cashfree / HDFC / ICICI API
        String transactionRef = initiateBankTransfer(
                payroll.getEmployee(),
                payroll.getNetSalary(),
                payroll.getBankAccount(),
                payroll.getIfscCode()
        );

        // Mark as PAID — amount deposited ✅
        payroll.setStatus(PayrollStatus.PAID);
        payroll.setPaymentDate(LocalDate.now());
        payroll.setPaymentReference(transactionRef);
        payroll.setPaymentMode("NEFT");
        payrollRepository.save(payroll);

        log.info("✅ SALARY PAID: Employee={} | Amount=₹{} | Ref={}",
                payroll.getEmployee().getEmployeeId(),
                payroll.getNetSalary(),
                transactionRef);

        // ── Send Payslip Email with PDF ───────────────────────────────────────
        try {
            emailService.sendPayslipEmail(payroll);   // @Async — non-blocking
            payroll.setPayslipSent(true);
            payrollRepository.save(payroll);
        } catch (Exception e) {
            log.warn("Payslip email failed for {} — payment still succeeded. Error: {}",
                    payroll.getEmployee().getEmailId(), e.getMessage());
        }
    }

    // GENERATE PAYROLL FOR ALL ACTIVE EMPLOYEES
    @Transactional
    public void generatePayrollForAllEmployees(LocalDate month) {

        Page<Employee> activePage = employeeRepository
                .findByEmploymentStatusAndDeletedFalse(
                        EmploymentStatus.ACTIVE,
                        PageRequest.of(0, Integer.MAX_VALUE, Sort.by("firstName")));

        List<Employee> activeEmployees = activePage.getContent();
        log.info("Generating payroll for {} employees for month: {}", activeEmployees.size(), month);

        for (Employee employee : activeEmployees) {
            try {
                generatePayrollForEmployee(employee, month);
            } catch (Exception e) {
                log.error("Payroll generation failed for {}: {}",
                        employee.getEmployeeId(), e.getMessage());
            }
        }
    }

    // GENERATE PAYROLL FOR A SINGLE EMPLOYEE
    @Transactional
    public Payroll generatePayrollForEmployee(Employee employee, LocalDate month) {

        // Skip if already generated for this month
        if (payrollRepository.existsByEmployeeIdAndPayrollMonth(employee.getId(), month)) {
            log.info("Payroll already exists for {} - {}", employee.getEmployeeId(), month);
            return payrollRepository
                    .findByEmployeeIdAndPayrollMonth(employee.getId(), month)
                    .orElseThrow(() -> new RuntimeException("Payroll not found"));
        }

        // ── Attendance for the month ──────────────────────────────────────────
        YearMonth ym         = YearMonth.of(month.getYear(), month.getMonth());
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd   = ym.atEndOfMonth();
        int workingDays      = calculateWorkingDays(monthStart, monthEnd);

        List<Attendance> attendances = attendanceRepository
                .findByEmployeeIdAndAttendanceDateBetween(
                        employee.getId(), monthStart, monthEnd);

        long presentDays = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT
                          || a.getStatus() == AttendanceStatus.WORK_FROM_HOME
                          || a.getStatus() == AttendanceStatus.HALF_DAY)
                .count();

        long leaveDays = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ON_LEAVE)
                .count();

        long absentDays = Math.max(workingDays - presentDays - leaveDays, 0);

        // ── Build Payroll object
        Payroll payroll = new Payroll();
        payroll.setEmployee(employee);
        payroll.setPayrollMonth(month);
        payroll.setWorkingDays(workingDays);
        payroll.setPresentDays((int) presentDays);
        payroll.setLeaveDays((int) leaveDays);
        payroll.setAbsentDays((int) absentDays);
        payroll.setBankAccount(employee.getAccountNo());
        payroll.setIfscCode(employee.getIfscCode());
        payroll.setStatus(PayrollStatus.PENDING);
        payroll.setProcessedBy("SYSTEM");

        // ── Calculate all salary components 
        calculationService.calculate(employee, payroll);

        return payrollRepository.save(payroll);
    }
    // APPROVE PAYROLL — HR manually approves before payment
    @Transactional
    public Payroll approvePayroll(Long payrollId, String approvedBy) {
        Payroll payroll = getPayrollById(payrollId);

        if (payroll.getStatus() != PayrollStatus.PENDING) {
            throw new RuntimeException(
                "Only PENDING payroll can be approved. Current status: " + payroll.getStatus());
        }

        payroll.setStatus(PayrollStatus.APPROVED);
        payroll.setApprovedBy(approvedBy);
        return payrollRepository.save(payroll);
    }
    // MANUAL PAYMENT — Admin triggers payment for a single payroll
    @Transactional
    public Payroll manualProcessPayment(Long payrollId, String approvedBy) {
        Payroll payroll = getPayrollById(payrollId);

        if (payroll.getStatus() != PayrollStatus.APPROVED) {
            throw new RuntimeException(
                "Payroll must be APPROVED before processing. Current status: "
                        + payroll.getStatus());
        }

        payroll.setApprovedBy(approvedBy);
        processSinglePayment(payroll);
        return payroll;
    }

    // PUT ON HOLD
    @Transactional
    public Payroll holdPayroll(Long payrollId, String reason) {
        Payroll payroll = getPayrollById(payrollId);

        if (payroll.getStatus() == PayrollStatus.PAID) {
            throw new RuntimeException("Cannot hold a payroll that is already PAID");
        }

        payroll.setStatus(PayrollStatus.ON_HOLD);
        payroll.setRemarks(reason);
        return payrollRepository.save(payroll);
    }

    // RETRY FAILED PAYMENT
    @Transactional
    public Payroll retryFailedPayment(Long payrollId) {
        Payroll payroll = getPayrollById(payrollId);

        if (payroll.getStatus() != PayrollStatus.FAILED) {
            throw new RuntimeException("Only FAILED payroll can be retried");
        }

        payroll.setStatus(PayrollStatus.APPROVED);
        payroll.setRemarks(null);
        payrollRepository.save(payroll);

        processSinglePayment(payroll);
        return payroll;
    }

   
    // GET PAYROLL BY ID
    public Payroll getPayrollById(Long payrollId) {
        return payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Payroll not found with id: " + payrollId));
    }

    // GET EMPLOYEE PAYROLL HISTORY
    public List<Payroll> getEmployeePayrollHistory(Long employeeId) {
        return payrollRepository.findByEmployeeId(employeeId);
    }

    // GET PAYROLL BY MONTH
    public Page<Payroll> getPayrollByMonth(LocalDate month, int page, int size) {
        return payrollRepository.findByPayrollMonth(
                month, PageRequest.of(page, size, Sort.by("employee.firstName")));
    }

    // GET SPECIFIC EMPLOYEE + MONTH PAYROLL
    public Optional<Payroll> getPayroll(Long employeeId, LocalDate month) {
        return payrollRepository.findByEmployeeIdAndPayrollMonth(employeeId, month);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BANK TRANSFER — Replace body with real bank API in production
    // ─────────────────────────────────────────────────────────────────────────
    private String initiateBankTransfer(Employee employee, BigDecimal amount,
                                         String accountNo, String ifscCode) {
        // ══════════════════════════════════════════════════════════════════════
        // PRODUCTION: Replace with actual bank API
        //
        // RazorpayX example:
        //   POST https://api.razorpay.com/v1/payouts
        //   Body: { fund_account_id, amount (paise), mode: "NEFT", purpose: "salary" }
        //
        // Cashfree Payouts example:
        //   POST https://payout-api.cashfree.com/payout/v1/requestTransfer
        //
        // Response will contain real transaction reference ID
        // ══════════════════════════════════════════════════════════════════════

        log.info("BANK TRANSFER → Employee: {} | Account: {} | Amount: ₹{} | IFSC: {}",
                employee.getEmployeeId(), maskAccount(accountNo), amount, ifscCode);

        // Simulate transaction reference
        return "TXN" + UUID.randomUUID().toString()
                           .replace("-", "")
                           .substring(0, 12)
                           .toUpperCase();
    }

    // HELPERS

    // Count working days (Mon–Sat, skip Sundays)
    private int calculateWorkingDays(LocalDate start, LocalDate end) {
        int count = 0;
        LocalDate date = start;
        while (!date.isAfter(end)) {
            if (date.getDayOfWeek().getValue() != 7) count++;
            date = date.plusDays(1);
        }
        return count;
    }

    // Mask account number for logs
    private String maskAccount(String accountNo) {
        if (accountNo == null || accountNo.length() < 4) return "****";
        return "****" + accountNo.substring(accountNo.length() - 4);
    }
}