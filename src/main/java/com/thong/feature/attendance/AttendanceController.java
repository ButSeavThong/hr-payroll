package com.thong.feature.attendance;

import com.thong.domain.Employee;
import com.thong.feature.attendance.dto.AttendanceResponse;
import com.thong.feature.attendance.dto.CheckInRequest;
import com.thong.feature.employee.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final EmployeeRepository employeeRepository;

    // Resolve JWT username → employee ID (used on every secured endpoint)
    private Integer resolveEmployeeId(Authentication authentication) {

        String email = authentication.getName();
        // getName() returns JWT subject (sub)
        log.info("Resolving employee id {}", email);
        return employeeRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("No employee profile found"))
                .getId();
    }

    // POST /api/v1/attendances/check-in done !
    @PostMapping("/check-in")
    @PreAuthorize("hasAnyAuthority('SCOPE_EMPLOYEE', 'SCOPE_ADMIN')")
    public ResponseEntity<AttendanceResponse> checkIn(
            Authentication authentication,
            @RequestBody(required = false) CheckInRequest request) {

        Integer empId = resolveEmployeeId(authentication);

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("No employee profile found"));
            log.info("Checking in employee id  {}", employee.getId());
        CheckInRequest req = (request != null)
                ? request
                : new CheckInRequest(null);

        return ResponseEntity.ok(attendanceService.checkIn(employee.getId(), req));
    }

    // POST /api/attendance/check-out done!
    @PostMapping("/check-out")
    @PreAuthorize("hasAnyAuthority('SCOPE_EMPLOYEE', 'SCOPE_ADMIN')")
    public ResponseEntity<AttendanceResponse> checkOut(
            Authentication authentication) {
        return ResponseEntity.ok(attendanceService.checkOut(resolveEmployeeId(authentication)));
    }

    // GET /api/attendance/my — employee sees only their own records done !
    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('SCOPE_EMPLOYEE', 'SCOPE_ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> getMyAttendance(
            Authentication authentication) {
        return ResponseEntity.ok(attendanceService.getMyAttendance(resolveEmployeeId(authentication)));
    }

    // GET /api/attendance — admin sees all records done !
    @GetMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> getAllAttendance() {
        return ResponseEntity.ok(attendanceService.getAllAttendance());
    }
}