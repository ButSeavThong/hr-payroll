// ── LeaveRequest.java ─────────────────────────────────────────────────────────
package com.thong.feature.leave.dto;


import com.thong.utils.LeaveType;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record LeaveRequest(

    @NotNull(message = "Leave type is required")
    LeaveType leaveType,

    @NotNull(message = "Start date is required")
    LocalDate startDate,

    @NotNull(message = "End date is required")
    LocalDate endDate,

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    String reason
) {}