package com.thong.domain;

import jakarta.persistence.*;
import lombok.*;

// domain/LeaveBalance.java
@Entity
@Table(name = "leave_balances",
  uniqueConstraints = @UniqueConstraint(
    columnNames = {"employee_id", "year"}  // one balance record per employee per year
  )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private Integer year;  // e.g. 2026

    // ── Annual Leave ──────────────────────────────────────
    @Builder.Default
    @Column(name = "annual_leave_total")
    private Integer annualLeaveTotal = 10;       // entitled days

    @Builder.Default
    @Column(name = "annual_leave_used")
    private Integer annualLeaveUsed = 0;         // days consumed

    // ── Sick Leave ────────────────────────────────────────
    @Builder.Default
    @Column(name = "sick_leave_total")
    private Integer sickLeaveTotal = 7;          // entitled days

    @Builder.Default
    @Column(name = "sick_leave_used")
    private Integer sickLeaveUsed = 0;           // days consumed

    // ── Derived (not stored) ──────────────────────────────
    @Transient
    public Integer getAnnualLeaveRemaining() {
        return annualLeaveTotal - annualLeaveUsed;
    }

    @Transient
    public Integer getSickLeaveRemaining() {
        return sickLeaveTotal - sickLeaveUsed;
    }
}