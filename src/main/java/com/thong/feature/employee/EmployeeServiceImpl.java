package com.thong.feature.employee;


import com.thong.domain.Employee;
import com.thong.domain.LeaveBalance;
import com.thong.domain.Role;
import com.thong.domain.User;
import com.thong.feature.employee.dto.*;
import com.thong.feature.leave.LeaveBalanceRepository;
import com.thong.feature.role.RoleRepository;
import com.thong.feature.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final EmployeeMapper employeeMapper;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public EmployeeResponse onboardEmployee(CreateEmployeeWithAccountRequest request) {

        if (!request.password().equals(request.confirmedPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("Email already registered: " + request.email());
        }

        // create user object
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(request.password())
                .dob(request.dob())
                .gender(request.gender())
                .build();
        user.setIsAccountNonLocked(true);
        user.setIsAccountNonExpired(true);
        user.setIsCredentialsNonExpired(true);
        user.setIsBlocked(false);
        user.setIsDeleted(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setProfileImage("user-avatar.png");
        user.setPassword(passwordEncoder.encode(user.getPassword()));


        Role employeeRole = roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("EMPLOYEE role not found"));
        user.setRoles(List.of(employeeRole));
        User savedUser = userRepository.save(user);


        // create employee object
        Employee employee = Employee.builder()
                .user(savedUser)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .department(request.department())
                .position(request.position())
                .baseSalary(request.baseSalary())
                .hireDate(request.hireDate() != null ? request.hireDate() : LocalDate.now())
                .isActive(true)
                .build();
        Employee savedEmployee = employeeRepository.save(employee);


        // create object leave balances
        LeaveBalance balance = LeaveBalance.builder()
                .employee(savedEmployee)
                .year(LocalDate.now().getYear())
                .annualLeaveTotal(10)
                .sickLeaveTotal(7)
                .build();


        leaveBalanceRepository.save(balance);

        log.info("Onboarded new employee: {} (userId={}, employeeId={})",
                request.email(), savedUser.getId(), savedEmployee.getId());

        return employeeMapper.toResponse(savedEmployee);


    }

    @Override
    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        // Business rule: one User can only have ONE employee profile
        if (employeeRepository.existsByUserId(request.userId())) {
            throw new IllegalStateException("User already has an employee profile");
        }

        var user = userRepository.findById(request.userId())
            .orElseThrow(() -> new RuntimeException("User not found with id: " + request.userId()));

        var employee = Employee.builder()
            .user(user)
            .firstName(request.firstName())
            .lastName(request.lastName())
            .department(request.department())
            .position(request.position())
            .baseSalary(request.baseSalary())
            .hireDate(request.hireDate())
            .isActive(true)
            .build();

        var savedEmployee = employeeRepository.save(employee);
        // Auto-create leave balance for current year
        var balance = LeaveBalance.builder()
                .employee(savedEmployee)
                .year(LocalDate.now().getYear())
                .annualLeaveTotal(10)
                .sickLeaveTotal(7)
                .build();

        leaveBalanceRepository.save(balance);

        return employeeMapper.toResponse(savedEmployee);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Integer id, UpdateEmployeeRequest request) {
        var employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setDepartment(request.department());
        employee.setPosition(request.position());
        if (request.baseSalary() != null) {
            employee.setBaseSalary(request.baseSalary());
        }

        return employeeMapper.toResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional(readOnly = true) // readOnly = JPA won't track state changes -> better performance
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll()
            .stream()
            .map(employeeMapper::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Integer id) {
        return employeeMapper.toResponse(
            employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getMyProfile(Integer userId) {
        // Security: userId comes from JWT context, not from request param
        return employeeMapper.toResponse(
            employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No employee profile found for this user"))
        );
    }


}