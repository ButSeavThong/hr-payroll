// ── LeaveReviewRequest.java ───────────────────────────────────────────────────
package com.thong.feature.leave.dto;


import com.thong.utils.LeaveStatus;
import jakarta.validation.constraints.NotNull;

public record LeaveReviewRequest(

        @NotNull(message = "Status is required")
        LeaveStatus status,   // APPROVED or REJECTED only

        String adminNote      // optional note to employee
) {}