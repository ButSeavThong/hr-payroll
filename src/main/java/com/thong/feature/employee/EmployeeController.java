package com.thong.feature.employee;

import com.thong.domain.Employee;
import com.thong.domain.User;
import com.thong.feature.employee.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;

    // POST /api/employees — Admin only / done !
    @PostMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(employeeService.createEmployee(request));
    }

    // PUT /api/employees/{id} — Admin only / done !
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    // GET /api/employees — Admin sees all / done !
    @GetMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    // GET /api/employees/{id} — Admin only / done!
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Integer id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    // GET /api/employees/me — Employee sees own profile only / done !
    // Identity comes from JWT — NEVER from a URL param (security risk)
    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_EMPLOYEE')")
    public ResponseEntity<EmployeeResponse> getMyProfile() {
        // Get the email from JWT token (subject)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String email = authentication.getName(); // This is the 'sub' claim from JWT
        // Find user by email
        Employee employee = employeeRepository.findByUser_Email(email).
                orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found!"));

        return ResponseEntity.ok(employeeService.getMyProfile(employee.getUser().getId()));
    }

    @PostMapping("/onboard")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    @Transactional  // if employee creation fails → user rolls back too
    public ResponseEntity<EmployeeResponse> onboardEmployee(
            @Valid @RequestBody CreateEmployeeWithAccountRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(employeeService.onboardEmployee(request));
    }


}