package com.thong.feature.employee.dto;

import com.thong.domain.Employee;
import org.springframework.stereotype.Component;

// Converts Entity <-> DTO. Isolated here so Service stays clean.
@Component
public class EmployeeMapper {

    public EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
            employee.getId(),
            employee.getUser().getId(),
            employee.getUser().getUsername(),
            employee.getUser().getEmail(),
            employee.getFirstName(),
            employee.getLastName(),
            employee.getDepartment(),
            employee.getPosition(),
            employee.getBaseSalary(),
            employee.getHireDate(),
            employee.getIsActive(),
            employee.getCreatedAt(),
                employee.getUser().getProfileImage()
        );
    }
}