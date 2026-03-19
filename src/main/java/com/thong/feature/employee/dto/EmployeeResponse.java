package com.thong.feature.employee.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Only exposes what the client needs — no passwords, no internal join details
public record EmployeeResponse(
    Integer id,
    Integer userId,
    String username,
    String email,
    String firstName,
    String lastName,
    String department,
    String position,
    BigDecimal baseSalary,
    LocalDate hireDate,
    Boolean isActive,
    LocalDateTime createdAt,
    String profileImage
) {}