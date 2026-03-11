package com.thong.feature.payroll.dto;

import java.math.BigDecimal;

// Add unpaidDeduction field to PayrollResponse
public record PayrollResponse(
        Integer id,
        Integer employeeId,
        String employeeName,
        String month,
        BigDecimal baseSalary,
        BigDecimal overtimePay,
        BigDecimal unpaidLeaveDeduction,  //  new field
        Integer unpaidLeaveDays,
        BigDecimal tax,
        BigDecimal netSalary,
        String status
) {}