package com.thong.feature.employee.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

// New combined request DTO
public record CreateEmployeeWithAccountRequest(
        // User fields
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be less than 100 characters")
        String username,


        @NotBlank(message = "Email is required")
        @Email
        String email,

        @NotBlank(message = "Password is required")
        String password,

        @NotBlank(message = "Confirmed password is required")
        String confirmedPassword,

        @NotBlank(message = "Gender is required")
        String gender,

        @NotNull(message = "Date of birth is required")
        LocalDate dob,


        // Employee fields
        @NotBlank(message = "First name is required")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100)
        String lastName,

        String department,
        String position,

        @NotNull(message = "Base salary is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be positive")
        BigDecimal baseSalary,

        LocalDate hireDate
) {}
