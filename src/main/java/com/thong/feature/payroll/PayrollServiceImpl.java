package com.thong.feature.payroll;

import com.thong.domain.Employee;
import com.thong.domain.Leave;
import com.thong.domain.Payroll;
import com.thong.feature.attendance.AttendanceRepository;
import com.thong.feature.employee.EmployeeRepository;
import com.thong.feature.leave.LeaveRepository;
import com.thong.feature.payroll.dto.GeneratePayrollRequest;
import com.thong.feature.payroll.dto.PayrollResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository      payrollRepository;
    private final EmployeeRepository     employeeRepository;
    private final AttendanceRepository   attendanceRepository;
    private final LeaveRepository        leaveRepository;       // ✅ new
    private final PayrollMapper          payrollMapper;

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final BigDecimal WORKING_HOURS_PER_MONTH = BigDecimal.valueOf(160); // 8h × 20 days
    private static final BigDecimal OVERTIME_MULTIPLIER     = BigDecimal.valueOf(1.5);
    private static final BigDecimal TAX_RATE                = BigDecimal.valueOf(0.10);
    private static final double     STANDARD_HOURS_PER_DAY  = 8.0;

    // ── Generate Payroll ──────────────────────────────────────────────────────
    @Override
    @Transactional
    public List<PayrollResponse> generatePayroll(GeneratePayrollRequest request) {
        List<Employee> employees;

        if (request.employeeId() != null) {
            employees = List.of(
                    employeeRepository.findById(request.employeeId())
                            .orElseThrow(() -> new RuntimeException("Employee not found"))
            );
        } else {
            employees = employeeRepository.findAll()
                    .stream()
                    .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                    .toList();
        }

        List<PayrollResponse> results = new ArrayList<>();

        for (Employee emp : employees) {
            // Skip if already generated
            if (payrollRepository.existsByEmployeeIdAndMonth(emp.getId(), request.month())) {
                log.info("Payroll already exists for employee {} month {}", emp.getId(), request.month());
                continue;
            }
            Payroll payroll = calculatePayroll(emp, request.month());
            results.add(payrollMapper.toResponse(payrollRepository.save(payroll)));
        }

        return results;
    }

    // ── Core Calculation ──────────────────────────────────────────────────────
    private Payroll calculatePayroll(Employee emp, String month) {
        BigDecimal baseSalary = emp.getBaseSalary();

        // Hourly rate = baseSalary ÷ 160
        BigDecimal hourlyRate = baseSalary.divide(
                WORKING_HOURS_PER_MONTH, 10, RoundingMode.HALF_UP
        );

        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate   = yearMonth.atEndOfMonth();

        // ── Overtime from attendance ──────────────────────────────────────────
        var attendances = attendanceRepository
                .findByEmployeeIdAndDateBetween(emp.getId(), startDate, endDate);

        double totalOvertimeHours = attendances.stream()
                .mapToDouble(a -> a.getOvertimeHours() != null ? a.getOvertimeHours() : 0.0)
                .sum();

        BigDecimal overtimePay = hourlyRate
                .multiply(BigDecimal.valueOf(totalOvertimeHours))
                .multiply(OVERTIME_MULTIPLIER)
                .setScale(2, RoundingMode.HALF_UP);

        // ── Unpaid leave deduction ────────────────────────────────────────────
        // dailyRate = hourlyRate × 8 hours
        BigDecimal dailyRate = hourlyRate
                .multiply(BigDecimal.valueOf(STANDARD_HOURS_PER_DAY))
                .setScale(2, RoundingMode.HALF_UP);

        // ✅ Find approved unpaid leaves in this month
        List<Leave> unpaidLeaves = leaveRepository
                .findApprovedUnpaidLeavesInMonth(emp.getId(), startDate, endDate);

        // ✅ Count total working days from all unpaid leaves
        int unpaidLeaveDays = unpaidLeaves.stream()
                .mapToInt(leave -> countWorkingDaysInMonth(
                        leave.getStartDate(),
                        leave.getEndDate(),
                        startDate,
                        endDate
                ))
                .sum();

        // ✅ Deduction = dailyRate × unpaidDays
        BigDecimal unpaidDeduction = unpaidLeaveDays > 0
                ? dailyRate
                .multiply(BigDecimal.valueOf(unpaidLeaveDays))
                .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        log.info("Employee {} | base={} overtime={} unpaidDays={} unpaidDeduction={}",
                emp.getId(), baseSalary, overtimePay, unpaidLeaveDays, unpaidDeduction);

        // ── Gross = base + overtime - unpaid deduction ────────────────────────
        BigDecimal gross = baseSalary
                .add(overtimePay)
                .subtract(unpaidDeduction);

        // Guard: gross can never be negative
        if (gross.compareTo(BigDecimal.ZERO) < 0) {
            gross = BigDecimal.ZERO;
        }

        // ── Tax = gross × 10% ─────────────────────────────────────────────────
        BigDecimal tax = gross
                .multiply(TAX_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        // ── Net = gross - tax ─────────────────────────────────────────────────
        BigDecimal netSalary = gross
                .subtract(tax)
                .setScale(2, RoundingMode.HALF_UP);

        return Payroll.builder()
                .employee(emp)
                .month(month)
                .baseSalary(baseSalary)
                .overtimePay(overtimePay)
                .unpaidLeaveDeduction(unpaidDeduction)
                .unpaidLeaveDays(unpaidLeaveDays)        // ✅ new field
                .tax(tax)
                .netSalary(netSalary)
                .status("GENERATED")
                .build();
    }

    // ── Count working days clamped to month boundary ──────────────────────────────
    private int countWorkingDaysInMonth(
            LocalDate leaveStart,
            LocalDate leaveEnd,
            LocalDate monthStart,
            LocalDate monthEnd) {

        // Clamp leave range to this month only
        LocalDate effectiveStart = leaveStart.isBefore(monthStart) ? monthStart : leaveStart;
        LocalDate effectiveEnd   = leaveEnd.isAfter(monthEnd)      ? monthEnd   : leaveEnd;

        if (effectiveStart.isAfter(effectiveEnd)) return 0;

        int days = 0;
        LocalDate current = effectiveStart;
        while (!current.isAfter(effectiveEnd)) {
            switch (current.getDayOfWeek()) {
                case SATURDAY, SUNDAY -> { /* skip weekends */ }
                default -> days++;
            }
            current = current.plusDays(1);
        }
        return days;
    }

    // ── Mark as Paid ──────────────────────────────────────────────────────────
    @Override
    @Transactional
    public PayrollResponse markAsPaid(Integer payrollId) {
        var payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Payroll not found"));

        if (payroll.getStatus() == "PAID") {
            throw new IllegalStateException("Payroll is already marked as PAID");
        }

        payroll.setStatus("PAID");
        return payrollMapper.toResponse(payrollRepository.save(payroll));
    }

    // ── Get All Payrolls (Admin) ───────────────────────────────────────────────
    @Override
    public List<PayrollResponse> getAllPayrolls(String month) {
        return payrollRepository.findByMonth(month)
                .stream()
                .map(payrollMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollResponse> getMyPayrolls(Integer employeeId) {
        return payrollRepository.findByEmployeeId(employeeId)
                .stream()
                .map(payrollMapper::toResponse)
                .toList();
    }
}