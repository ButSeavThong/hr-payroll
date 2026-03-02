// ── LeaveServiceImpl.java ─────────────────────────────────────────────────────
package com.thong.feature.leave;

import com.thong.domain.Leave;

import com.thong.feature.employee.EmployeeRepository;
import com.thong.feature.leave.dto.LeaveRequest;
import com.thong.feature.leave.dto.LeaveResponse;
import com.thong.feature.leave.dto.LeaveReviewRequest;
import com.thong.utils.LeaveStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveMapper leaveMapper;

    // ── Employee: request leave ───────────────────────────────────────────────
    @Override
    @Transactional
    public LeaveResponse requestLeave(Integer employeeId, LeaveRequest request) {

        // Validate dates
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        if (request.startDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot request leave for past dates");
        }

        // Check overlapping leave
        if (leaveRepository.hasOverlappingLeave(
                employeeId, request.startDate(), request.endDate())) {
            throw new IllegalStateException(
                "You already have a leave request for these dates");
        }

        var employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Calculate working days (exclude weekends)
        int totalDays = calculateWorkingDays(request.startDate(), request.endDate());

        var leave = Leave.builder()
            .employee(employee)
            .leaveType(request.leaveType())
            .startDate(request.startDate())
            .endDate(request.endDate())
            .totalDays(totalDays)
            .reason(request.reason())
            .status(LeaveStatus.PENDING)
            .build();

        return leaveMapper.toResponse(leaveRepository.save(leave));
    }

    // ── Admin: approve or reject ──────────────────────────────────────────────
    @Override
    @Transactional
    public LeaveResponse reviewLeave(Integer leaveId, LeaveReviewRequest request) {

        var leave = leaveRepository.findById(leaveId)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException(
                "Leave request has already been " + leave.getStatus().name().toLowerCase());
        }

        // Only APPROVED or REJECTED allowed
        if (request.status() == LeaveStatus.PENDING) {
            throw new IllegalArgumentException("Status must be APPROVED or REJECTED");
        }

        leave.setStatus(request.status());
        leave.setAdminNote(request.adminNote());
        leave.setReviewedAt(LocalDateTime.now());

        return leaveMapper.toResponse(leaveRepository.save(leave));
    }

    // ── Employee: my leaves ───────────────────────────────────────────────────
    @Override
    public List<LeaveResponse> getMyLeaves(Integer employeeId) {
        return leaveRepository
            .findByEmployeeIdOrderByCreatedAtDesc(employeeId)
            .stream()
            .map(leaveMapper::toResponse)
            .toList();
    }

    // ── Admin: all leaves ─────────────────────────────────────────────────────
    @Override
    public List<LeaveResponse> getAllLeaves() {
        return leaveRepository
            .findAllByOrderByCreatedAtDesc()
            .stream()
            .map(leaveMapper::toResponse)
            .toList();
    }

    // ── Admin: pending only ───────────────────────────────────────────────────
    @Override
    public List<LeaveResponse> getPendingLeaves() {
        return leaveRepository
            .findByStatusOrderByCreatedAtDesc(LeaveStatus.PENDING)
            .stream()
            .map(leaveMapper::toResponse)
            .toList();
    }

    // ── Helper: count working days (skip Sat/Sun) ─────────────────────────────
    private int calculateWorkingDays(LocalDate start, LocalDate end) {
        int days = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            DayOfWeek dow = current.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                days++;
            }
            current = current.plusDays(1);
        }
        return days;
    }
}