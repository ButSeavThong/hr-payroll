// ── LeaveResponse.java ────────────────────────────────────────────────────────
package com.thong.feature.leave.dto;


import com.thong.utils.LeaveStatus;
import com.thong.utils.LeaveType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeaveResponse(
    Integer id,
    Integer employeeId,
    String employeeName,        // firstName + lastName
    LeaveType leaveType,
    LocalDate startDate,
    LocalDate endDate,
    Integer totalDays,
    String reason,
    LeaveStatus status,
    String adminNote,
    LocalDateTime reviewedAt,
    LocalDateTime createdAt
) {}