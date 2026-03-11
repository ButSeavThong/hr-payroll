package com.thong.feature.leave.dto;

// LeaveBalanceResponse.java DTO
public record LeaveBalanceResponse(
    Integer year,
    Integer annualLeaveTotal,
    Integer annualLeaveUsed,
    Integer annualLeaveRemaining,
    Integer sickLeaveTotal,
    Integer sickLeaveUsed,
    Integer sickLeaveRemaining
) {}