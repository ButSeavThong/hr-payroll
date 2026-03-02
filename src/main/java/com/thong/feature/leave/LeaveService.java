// ── LeaveService.java ─────────────────────────────────────────────────────────
package com.thong.feature.leave;

import com.thong.feature.leave.dto.LeaveRequest;
import com.thong.feature.leave.dto.LeaveResponse;
import com.thong.feature.leave.dto.LeaveReviewRequest;
import java.util.List;

public interface LeaveService {
    LeaveResponse requestLeave(Integer employeeId, LeaveRequest request);
    LeaveResponse reviewLeave(Integer leaveId, LeaveReviewRequest request);  // admin
    List<LeaveResponse> getMyLeaves(Integer employeeId);
    List<LeaveResponse> getAllLeaves();                                        // admin
    List<LeaveResponse> getPendingLeaves();                                   // admin
}