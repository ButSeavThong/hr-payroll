package com.thong.feature.employee;

import com.thong.feature.employee.dto.CreateEmployeeRequest;
import com.thong.feature.employee.dto.CreateEmployeeWithAccountRequest;
import com.thong.feature.employee.dto.EmployeeResponse;
import com.thong.feature.employee.dto.UpdateEmployeeRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Interface = contract. Controller depends on this, not the concrete class.
public interface EmployeeService {

    EmployeeResponse createEmployee(CreateEmployeeRequest request);
    EmployeeResponse updateEmployee(Integer id, UpdateEmployeeRequest request);
    List<EmployeeResponse> getAllEmployees();
    EmployeeResponse getEmployeeById(Integer id);
    // userId comes from JWT — employees can only see their own profile
    EmployeeResponse getMyProfile(Integer userId);
    EmployeeResponse onboardEmployee(CreateEmployeeWithAccountRequest request);


}