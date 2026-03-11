package com.thong.feature.payroll;

import com.thong.domain.Payroll;
import com.thong.feature.payroll.dto.PayrollResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PayrollMapper {

    public PayrollResponse toResponse(Payroll p) {
        return new PayrollResponse(
                p.getId(),
                p.getEmployee().getId(),
                p.getEmployee().getFirstName() + " " + p.getEmployee().getLastName(),
                p.getMonth(),
                p.getBaseSalary(),
                p.getOvertimePay(),
                p.getUnpaidLeaveDeduction() != null ? p.getUnpaidLeaveDeduction() : BigDecimal.ZERO,
                p.getUnpaidLeaveDays()      != null ? p.getUnpaidLeaveDays()      : 0,
                p.getTax(),
                p.getNetSalary(),
                p.getStatus()
        );
    }
}