package com.thong.feature.leave;

import com.thong.domain.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Integer> {

    // Find balance for a specific employee + year
    Optional<LeaveBalance> findByEmployeeIdAndYear(Integer employeeId, Integer year);

    // All balances for one employee (all years)
    List<LeaveBalance> findByEmployeeIdOrderByYearDesc(Integer employeeId);

    // Check if balance already exists
    boolean existsByEmployeeIdAndYear(Integer employeeId, Integer year);
}
