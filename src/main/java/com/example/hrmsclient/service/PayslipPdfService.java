package com.example.hrmsclient.service;

import com.example.hrmsclient.entity.Payroll;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;


@Service
public class PayslipPdfService {

    @Value("${app.upload.payslip-dir}")
    private String payslipDir;

    @Value("${app.company.name}")
    private String companyName;

    // ── Brand Colors (Zoho-inspired: clean, corporate, trustworthy) ───────────
    private static final BaseColor COL_HEADER_BG   = new BaseColor(24,  43,  73);   // deep navy
    private static final BaseColor COL_HEADER_TEXT = BaseColor.WHITE;
    private static final BaseColor COL_ACCENT      = new BaseColor(0,  114, 187);   // corporate blue
    private static final BaseColor COL_ACCENT_LITE = new BaseColor(232, 244, 253);  // very light blue
    private static final BaseColor COL_SECTION_BG  = new BaseColor(245, 247, 250);  // off-white row bg
    private static final BaseColor COL_LABEL       = new BaseColor(90,  100, 120);  // muted label grey
    private static final BaseColor COL_VALUE       = new BaseColor(20,  30,  48);   // near-black
    private static final BaseColor COL_BORDER      = new BaseColor(213, 220, 230);  // light border
    private static final BaseColor COL_GREEN       = new BaseColor(16,  142,  80);  // earnings green
    private static final BaseColor COL_RED         = new BaseColor(197,  40,  40);  // deductions red
    private static final BaseColor COL_NET_BG      = new BaseColor(0,  114, 187);   // net banner bg
    private static final BaseColor COL_ATT_BG      = new BaseColor(250, 251, 253);  // attendance box

    // ── Font Declarations ─────────────────────────────────────────────────────
    // (iText 5 built-in Helvetica — swap for embedded font if needed)
    private static final Font F_CO_NAME  = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD,   BaseColor.WHITE);
    private static final Font F_CO_TAG   = new Font(Font.FontFamily.HELVETICA,  8, Font.NORMAL, new BaseColor(180, 200, 220));
    private static final Font F_PS_TITLE = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD,   BaseColor.WHITE);
    private static final Font F_PS_MONTH = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, new BaseColor(180, 200, 220));
    private static final Font F_SEC_HEAD = new Font(Font.FontFamily.HELVETICA,  8, Font.BOLD,   COL_ACCENT);
    private static final Font F_LABEL    = new Font(Font.FontFamily.HELVETICA,  8, Font.NORMAL, COL_LABEL);
    private static final Font F_VALUE    = new Font(Font.FontFamily.HELVETICA,  8, Font.BOLD,   COL_VALUE);
    private static final Font F_TH       = new Font(Font.FontFamily.HELVETICA,  8, Font.BOLD,   BaseColor.WHITE);
    private static final Font F_ROW_LBL  = new Font(Font.FontFamily.HELVETICA,  8, Font.NORMAL, COL_VALUE);
    private static final Font F_ROW_AMT  = new Font(Font.FontFamily.HELVETICA,  8, Font.BOLD,   COL_VALUE);
    private static final Font F_TOT_LBL  = new Font(Font.FontFamily.HELVETICA,  8, Font.BOLD,   COL_VALUE);
    private static final Font F_GROSS    = new Font(Font.FontFamily.HELVETICA,  9, Font.BOLD,   COL_GREEN);
    private static final Font F_DED      = new Font(Font.FontFamily.HELVETICA,  9, Font.BOLD,   COL_RED);
    private static final Font F_NET_LBL  = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(200, 230, 255));
    private static final Font F_NET_AMT  = new Font(Font.FontFamily.HELVETICA, 15, Font.BOLD,   BaseColor.WHITE);
    private static final Font F_PAY_LBL  = new Font(Font.FontFamily.HELVETICA,  8, Font.NORMAL, COL_LABEL);
    private static final Font F_PAY_VAL  = new Font(Font.FontFamily.HELVETICA,  8, Font.BOLD,   COL_VALUE);
    private static final Font F_FOOTER   = new Font(Font.FontFamily.HELVETICA,  7, Font.ITALIC, new BaseColor(150, 160, 175));
    private static final Font F_ATT_NUM  = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD,   COL_ACCENT);
    private static final Font F_ATT_LBL  = new Font(Font.FontFamily.HELVETICA,  7, Font.NORMAL, COL_LABEL);

    // ─────────────────────────────────────────────────────────────────────────
    public File generatePayslip(Payroll payroll) throws Exception {

        File dir = new File(payslipDir);
        if (!dir.exists()) dir.mkdirs();

        String month    = payroll.getPayrollMonth().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String empId    = payroll.getEmployee().getEmployeeId();
        String filePath = payslipDir + empId + "_" + month + "_payslip.pdf";
        File   pdfFile  = new File(filePath);

        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(pdfFile));
        doc.open();

        addHeader(doc, payroll);
        addEmployeeInfo(doc, payroll);
        addAttendanceSummary(doc, payroll);
        addEarningsDeductions(doc, payroll);
        addNetBanner(doc, payroll);
        addPaymentDetails(doc, payroll);
        addFooter(doc);

        doc.close();
        return pdfFile;
    }

    // ── 1. HEADER ─────────────────────────────────────────────────────────────
    private void addHeader(Document doc, Payroll p) throws Exception {
        String monthYear = p.getPayrollMonth().format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        PdfPTable tbl = new PdfPTable(2);
        tbl.setWidthPercentage(100);
        tbl.setWidths(new float[]{65f, 35f});

        // Left: company name + tagline
        PdfPCell left = new PdfPCell();
        left.setBackgroundColor(COL_HEADER_BG);
        left.setBorder(Rectangle.NO_BORDER);
        left.setPadding(18);
        left.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph coName = new Paragraph(companyName != null ? companyName : "Company", F_CO_NAME);
        coName.setSpacingAfter(2);
        left.addElement(coName);
        left.addElement(new Phrase("Result Driven · Future Ready", F_CO_TAG));
        tbl.addCell(left);

        // Right: PAYSLIP label + month
        PdfPCell right = new PdfPCell();
        right.setBackgroundColor(COL_ACCENT);
        right.setBorder(Rectangle.NO_BORDER);
        right.setPadding(18);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        right.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph psLabel = new Paragraph("PAYSLIP", F_PS_TITLE);
        psLabel.setAlignment(Element.ALIGN_RIGHT);
        psLabel.setSpacingAfter(4);
        right.addElement(psLabel);

        Paragraph psMonth = new Paragraph(monthYear.toUpperCase(), F_PS_MONTH);
        psMonth.setAlignment(Element.ALIGN_RIGHT);
        right.addElement(psMonth);
        tbl.addCell(right);

        doc.add(tbl);
        doc.add(spacer(8));
    }

    // ── 2. EMPLOYEE INFORMATION ───────────────────────────────────────────────
    private void addEmployeeInfo(Document doc, Payroll p) throws Exception {
        sectionHeading(doc, "EMPLOYEE INFORMATION");

        PdfPTable tbl = new PdfPTable(4);
        tbl.setWidthPercentage(100);
        tbl.setWidths(new float[]{22f, 28f, 22f, 28f});
        tbl.setSpacingBefore(4);

        infoCell(tbl, "Employee ID",   p.getEmployee().getEmployeeId(),  true);
        infoCell(tbl, "Name",          p.getEmployee().getFullName(),     false);
        infoCell(tbl, "Department",    nvl(p.getEmployee().getDepartment()), true);
        infoCell(tbl, "Designation",   nvl(p.getEmployee().getDesignation()), false);
        infoCell(tbl, "Work Email",    nvl(p.getEmployee().getEmailId()),  true);
        infoCell(tbl, "Bank Account",  maskAccount(p.getBankAccount()),   false);
        infoCell(tbl, "IFSC Code",     nvl(p.getIfscCode()),              true);
        infoCell(tbl, "Pay Period",    p.getPayrollMonth()
                                        .format(DateTimeFormatter.ofPattern("MMMM yyyy")), false);

        doc.add(tbl);
        doc.add(spacer(10));
    }

    // ── 3. ATTENDANCE SUMMARY (4 highlight boxes) ─────────────────────────────
    private void addAttendanceSummary(Document doc, Payroll p) throws Exception {
        sectionHeading(doc, "ATTENDANCE SUMMARY");

        PdfPTable tbl = new PdfPTable(4);
        tbl.setWidthPercentage(100);
        tbl.setSpacingBefore(4);

        attBox(tbl, String.valueOf(p.getWorkingDays()),  "Working Days");
        attBox(tbl, String.valueOf(p.getPresentDays()),  "Present Days");
        attBox(tbl, String.valueOf(p.getLeaveDays()),    "Leave Days");
        attBox(tbl, String.valueOf(p.getAbsentDays()),   "Absent (LOP)");

        doc.add(tbl);
        doc.add(spacer(10));
    }

    // ── 4. EARNINGS & DEDUCTIONS (side-by-side) ───────────────────────────────
    private void addEarningsDeductions(Document doc, Payroll p) throws Exception {
        sectionHeading(doc, "EARNINGS & DEDUCTIONS");

        // Outer 2-column table
        PdfPTable outer = new PdfPTable(2);
        outer.setWidthPercentage(100);
        outer.setSpacingBefore(4);

        // ── LEFT: Earnings ──
        PdfPTable earn = new PdfPTable(2);
        earn.setWidths(new float[]{70f, 30f});
        earn.setWidthPercentage(100);

        // Header row
        earn.addCell(colHeader("EARNINGS"));
        earn.addCell(colHeader("AMOUNT (₹)"));

        // Rows
        Object[][] earnRows = {
            { "Basic Salary",      p.getBasicSalary()      },
            { "HRA",               p.getHra()              },
            { "Special Allowance", p.getSpecialAllowance() },
            { "Performance Pay",   p.getPerfPay()          },
            { "Weekend Work",      p.getWeekendWorkAmount() },
            { "Arrears",           p.getArrears()          },
            { "Reimbursement",     p.getReimbursement()    },
            { "FBP",               p.getFbp()              },
            { "Bonus",             p.getBonusAmount()       },
        };

        boolean alt = false;
        for (Object[] row : earnRows) {
            BaseColor bg = alt ? COL_SECTION_BG : BaseColor.WHITE;
            earn.addCell(dataCell((String) row[0], F_ROW_LBL, bg, Element.ALIGN_LEFT));
            earn.addCell(dataCell(fmt((BigDecimal) row[1]), F_ROW_AMT, bg, Element.ALIGN_RIGHT));
            alt = !alt;
        }

        // Gross total row
        earn.addCell(totalCell("Gross Salary", F_TOT_LBL, COL_ACCENT_LITE, Element.ALIGN_LEFT));
        earn.addCell(totalCell(fmt(p.getGrossSalary()), F_GROSS, COL_ACCENT_LITE, Element.ALIGN_RIGHT));

        PdfPCell earnWrapper = new PdfPCell(earn);
        earnWrapper.setBorder(Rectangle.NO_BORDER);
        earnWrapper.setPadding(0);
        earnWrapper.setPaddingRight(4);
        outer.addCell(earnWrapper);

        // ── RIGHT: Deductions ──
        PdfPTable ded = new PdfPTable(2);
        ded.setWidths(new float[]{70f, 30f});
        ded.setWidthPercentage(100);

        ded.addCell(colHeader("DEDUCTIONS"));
        ded.addCell(colHeader("AMOUNT (₹)"));

        Object[][] dedRows = {
            { "PF (Employee 12%)", p.getPfEmployee()      },
            { "Professional Tax",  p.getProfessionalTax() },
            { "TDS (Income Tax)",  p.getTds()             },
            { "Salary Advance",    p.getSalaryAdvance()   },
            { "Other Deduction",   p.getOtherDeduction()  },
        };

        alt = false;
        for (Object[] row : dedRows) {
            BaseColor bg = alt ? COL_SECTION_BG : BaseColor.WHITE;
            ded.addCell(dataCell((String) row[0], F_ROW_LBL, bg, Element.ALIGN_LEFT));
            ded.addCell(dataCell(fmt((BigDecimal) row[1]), F_ROW_AMT, bg, Element.ALIGN_RIGHT));
            alt = !alt;
        }

        // Pad deductions table to same height as earnings (add blank rows)
        int blankRows = earnRows.length - dedRows.length;
        for (int i = 0; i < blankRows; i++) {
            ded.addCell(dataCell("", F_ROW_LBL, BaseColor.WHITE, Element.ALIGN_LEFT));
            ded.addCell(dataCell("", F_ROW_AMT, BaseColor.WHITE, Element.ALIGN_RIGHT));
        }

        // Total deductions row
        ded.addCell(totalCell("Total Deductions", F_TOT_LBL, COL_ACCENT_LITE, Element.ALIGN_LEFT));
        ded.addCell(totalCell(fmt(p.getTotalDeductions()), F_DED, COL_ACCENT_LITE, Element.ALIGN_RIGHT));

        PdfPCell dedWrapper = new PdfPCell(ded);
        dedWrapper.setBorder(Rectangle.NO_BORDER);
        dedWrapper.setPadding(0);
        dedWrapper.setPaddingLeft(4);
        outer.addCell(dedWrapper);

        doc.add(outer);
        doc.add(spacer(8));
    }

    // ── 5. NET SALARY BANNER ──────────────────────────────────────────────────
    private void addNetBanner(Document doc, Payroll p) throws Exception {
        PdfPTable tbl = new PdfPTable(1);
        tbl.setWidthPercentage(100);
        tbl.setSpacingBefore(2);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COL_NET_BG);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(14);
        cell.setPaddingBottom(14);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph para = new Paragraph();
        para.setAlignment(Element.ALIGN_CENTER);
        para.add(new Chunk("NET SALARY CREDITED    ", F_NET_LBL));
        para.add(new Chunk("₹ " + fmt(p.getNetSalary()), F_NET_AMT));
        cell.addElement(para);
        tbl.addCell(cell);

        doc.add(tbl);
        doc.add(spacer(10));
    }

    // ── 6. PAYMENT DETAILS ────────────────────────────────────────────────────
    private void addPaymentDetails(Document doc, Payroll p) throws Exception {
        sectionHeading(doc, "PAYMENT DETAILS");

        PdfPTable tbl = new PdfPTable(4);
        tbl.setWidthPercentage(100);
        tbl.setWidths(new float[]{22f, 28f, 22f, 28f});
        tbl.setSpacingBefore(4);

        String payDate = p.getPaymentDate() != null
                ? p.getPaymentDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "—";
        String status  = p.getStatus() != null ? p.getStatus().name() : "—";

        infoCell(tbl, "Payment Date",    payDate,                          true);
        infoCell(tbl, "Transaction Ref", nvl(p.getPaymentReference()),     false);
        infoCell(tbl, "Payment Mode",    nvl(p.getPaymentMode()),          true);
        infoCell(tbl, "Status",          status,                           false);

        doc.add(tbl);
        doc.add(spacer(12));
    }

    // ── 7. FOOTER ─────────────────────────────────────────────────────────────
    private void addFooter(Document doc) throws Exception {
        // Top divider line
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell lineCell = new PdfPCell(new Phrase(""));
        lineCell.setBorderWidthTop(0.5f);
        lineCell.setBorderColorTop(COL_BORDER);
        lineCell.setBorderWidthBottom(0);
        lineCell.setBorderWidthLeft(0);
        lineCell.setBorderWidthRight(0);
        lineCell.setPaddingTop(0);
        lineCell.setPaddingBottom(0);
        line.addCell(lineCell);
        doc.add(line);

        // Footer text
        PdfPTable tbl = new PdfPTable(1);
        tbl.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(6);

        Paragraph p1 = new Paragraph(
            "This is a computer-generated payslip and does not require a signature.", F_FOOTER);
        p1.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(p1);

        Paragraph p2 = new Paragraph(
            "For queries or discrepancies, please contact your HR department.", F_FOOTER);
        p2.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(p2);

        tbl.addCell(cell);
        doc.add(tbl);
    }

    

    /** Blue underlined section heading */
    private void sectionHeading(Document doc, String title) throws Exception {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        PdfPCell c = new PdfPCell(new Phrase(title, F_SEC_HEAD));
        c.setBorderWidthBottom(1.2f);
        c.setBorderColorBottom(COL_ACCENT);
        c.setBorderWidthTop(0);
        c.setBorderWidthLeft(0);
        c.setBorderWidthRight(0);
        c.setPaddingBottom(5);
        c.setPaddingTop(2);
        t.addCell(c);
        doc.add(t);
    }

    /** Label + value info cell pair */
    private void infoCell(PdfPTable tbl, String label, String value, boolean shadeLabel) {
        PdfPCell lc = new PdfPCell(new Phrase(label, F_LABEL));
        lc.setBackgroundColor(shadeLabel ? COL_SECTION_BG : COL_ACCENT_LITE);
        lc.setBorderColor(COL_BORDER);
        lc.setBorderWidth(0.4f);
        lc.setPadding(6);
        tbl.addCell(lc);

        PdfPCell vc = new PdfPCell(new Phrase(value != null ? value : "—", F_VALUE));
        vc.setBackgroundColor(BaseColor.WHITE);
        vc.setBorderColor(COL_BORDER);
        vc.setBorderWidth(0.4f);
        vc.setPadding(6);
        tbl.addCell(vc);
    }

    /** Attendance highlight box */
    private void attBox(PdfPTable tbl, String number, String label) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COL_ATT_BG);
        cell.setBorderColor(COL_BORDER);
        cell.setBorderWidth(0.4f);
        cell.setPadding(10);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph num = new Paragraph(number, F_ATT_NUM);
        num.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(num);

        Paragraph lbl = new Paragraph(label, F_ATT_LBL);
        lbl.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(lbl);

        tbl.addCell(cell);
    }

    /** Column header cell (accent background, white bold text) */
    private PdfPCell colHeader(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, F_TH));
        c.setBackgroundColor(COL_ACCENT);
        c.setBorderColor(COL_BORDER);
        c.setBorderWidth(0.4f);
        c.setPaddingTop(6);
        c.setPaddingBottom(6);
        c.setPaddingLeft(6);
        c.setPaddingRight(6);
        c.setHorizontalAlignment(text.contains("AMOUNT") ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
        return c;
    }

    /** Regular data row cell */
    private PdfPCell dataCell(String text, Font font, BaseColor bg, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "", font));
        c.setBackgroundColor(bg);
        c.setBorderColor(COL_BORDER);
        c.setBorderWidth(0.4f);
        c.setPaddingTop(5);
        c.setPaddingBottom(5);
        c.setPaddingLeft(6);
        c.setPaddingRight(6);
        c.setHorizontalAlignment(align);
        return c;
    }

    /** Totals row cell (slightly taller, accent-lite background) */
    private PdfPCell totalCell(String text, Font font, BaseColor bg, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "", font));
        c.setBackgroundColor(bg);
        c.setBorderColor(COL_BORDER);
        c.setBorderWidth(0.5f);
        c.setPaddingTop(7);
        c.setPaddingBottom(7);
        c.setPaddingLeft(6);
        c.setPaddingRight(6);
        c.setHorizontalAlignment(align);
        return c;
    }

    private Paragraph spacer(float height) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingBefore(height / 2);
        p.setSpacingAfter(height / 2);
        return p;
    }

    private String fmt(BigDecimal val) {
        if (val == null) return "0.00";
        return String.format("%,.2f", val);
    }

    private String nvl(String val) {
        return (val != null && !val.isBlank()) ? val : "—";
    }

    private String maskAccount(String accountNo) {
        if (accountNo == null || accountNo.length() < 4) return "****";
        return "****" + accountNo.substring(accountNo.length() - 4);
    }
}