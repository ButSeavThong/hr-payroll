package com.thong.feature.leave;

import com.thong.feature.employee.EmployeeRepository;
import com.thong.feature.leave.dto.LeaveBalanceResponse;
import com.thong.feature.leave.dto.LeaveRequest;
import com.thong.feature.leave.dto.LeaveResponse;
import com.thong.feature.leave.dto.LeaveReviewRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final EmployeeRepository employeeRepository; // to resolve employeeId from JWT
    private final LeaveBalanceRepository leaveBalanceRepository;
    // ── Employee: submit leave request ────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_EMPLOYEE', 'SCOPE_ADMIN')")
    public ResponseEntity<LeaveResponse> requestLeave(
            @Valid @RequestBody LeaveRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Integer employeeId = resolveEmployeeId(jwt);

        log.info("Employee ID: {}", employeeId);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(leaveService.requestLeave(employeeId, request));
    }

    // ── Employee: my leave history ────────────────────────────────────────────
    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('SCOPE_EMPLOYEE', 'SCOPE_ADMIN')")
    public ResponseEntity<List<LeaveResponse>> getMyLeaves(
            @AuthenticationPrincipal Jwt jwt) {

        Integer employeeId = resolveEmployeeId(jwt);
        return ResponseEntity.ok(leaveService.getMyLeaves(employeeId));
    }

    // ── Admin: all leave requests ─────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    public ResponseEntity<List<LeaveResponse>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    // ── Admin: pending leaves only ────────────────────────────────────────────
    @GetMapping("/pending")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    public ResponseEntity<List<LeaveResponse>> getPendingLeaves() {
        return ResponseEntity.ok(leaveService.getPendingLeaves());
    }

    // ── Admin: approve or reject ──────────────────────────────────────────────
    @PatchMapping("/{id}/review")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    public ResponseEntity<LeaveResponse> reviewLeave(
            @PathVariable Integer id,
            @Valid @RequestBody LeaveReviewRequest request) {

        return ResponseEntity.ok(leaveService.reviewLeave(id, request));
    }

    // ── Helper: get employeeId from JWT email ─────────────────────────────────
    private Integer resolveEmployeeId(Jwt jwt) {
        String email = jwt.getSubject();
        return employeeRepository
            .findByUser_Email(email)
            .orElseThrow(() -> new RuntimeException("Employee profile not found"))
            .getId();
    }


    // GET /api/v1/leaves/balance — employee's own balance for current year
    @GetMapping("/balance")
    @PreAuthorize("hasAnyAuthority('SCOPE_EMPLOYEE', 'SCOPE_ADMIN')")
    public ResponseEntity<LeaveBalanceResponse> getMyBalance(
            @AuthenticationPrincipal Jwt jwt) {

        Integer employeeId = resolveEmployeeId(jwt);
        int year = LocalDate.now().getYear();

        var balance = leaveBalanceRepository
                .findByEmployeeIdAndYear(employeeId, year)
                .orElseThrow(() -> new RuntimeException("Leave balance not found"));

        return ResponseEntity.ok(new LeaveBalanceResponse(
                balance.getYear(),
                balance.getAnnualLeaveTotal(),
                balance.getAnnualLeaveUsed(),
                balance.getAnnualLeaveRemaining(),
                balance.getSickLeaveTotal(),
                balance.getSickLeaveUsed(),
                balance.getSickLeaveRemaining()
        ));
    }
}