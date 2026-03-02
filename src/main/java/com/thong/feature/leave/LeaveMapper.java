package com.thong.feature.leave;

import com.thong.domain.Leave;
import com.thong.feature.leave.dto.LeaveResponse;
import org.springframework.stereotype.Component;

@Component
public class LeaveMapper {

    public LeaveResponse toResponse(Leave leave) {
        return new LeaveResponse(
            leave.getId(),
            leave.getEmployee().getId(),
            leave.getEmployee().getFirstName() + " " + leave.getEmployee().getLastName(),
            leave.getLeaveType(),
            leave.getStartDate(),
            leave.getEndDate(),
            leave.getTotalDays(),
            leave.getReason(),
            leave.getStatus(),
            leave.getAdminNote(),
            leave.getReviewedAt(),
            leave.getCreatedAt()
        );
    }
}