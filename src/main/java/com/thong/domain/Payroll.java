package com.thong.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "payrolls",
        uniqueConstraints = {
                // Prevent duplicate payroll for same employee in same month
                @UniqueConstraint(columnNames = {"employee_id", "month"})
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // Format: "yyyy-MM" (e.g., "2024-06")
    @Column(nullable = false)
    private String month;

    // Snapshot of base salary at time of generation
    // If salary changes later, historical records remain accurate
    @Column(precision = 15, scale = 2)
    private BigDecimal baseSalary;

    // overtimeHours * hourlyRate * 1.5
    @Column(precision = 15, scale = 2)
    private BigDecimal overtimePay;

    // Simplified: 10% of gross
    @Column(precision = 15, scale = 2)
    private BigDecimal tax;

    // Net = baseSalary + overtimePay - tax
    @Column(precision = 15, scale = 2)
    private BigDecimal netSalary;

    // Lifecycle: GENERATED -> PAID
    @Column(nullable = false)
    private String status = "GENERATED";


    @Column(name = "unpaid_leave_deduction", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal unpaidLeaveDeduction = BigDecimal.ZERO;

    @Column(name = "unpaid_leave_days")
    @Builder.Default
    private Integer unpaidLeaveDays = 0;
}