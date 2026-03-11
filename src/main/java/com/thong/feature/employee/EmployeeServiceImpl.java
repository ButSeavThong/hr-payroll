package com.thong.feature.employee;


import com.thong.domain.Employee;
import com.thong.domain.LeaveBalance;
import com.thong.feature.employee.dto.CreateEmployeeRequest;
import com.thong.feature.employee.dto.EmployeeMapper;
import com.thong.feature.employee.dto.EmployeeResponse;
import com.thong.feature.employee.dto.UpdateEmployeeRequest;
import com.thong.feature.leave.LeaveBalanceRepository;
import com.thong.feature.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final EmployeeMapper employeeMapper;
    private final LeaveBalanceRepository leaveBalanceRepository;
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