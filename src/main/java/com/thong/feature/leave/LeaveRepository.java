package com.thong.feature.leave;

import com.thong.domain.Employee;
import com.thong.domain.Leave;
import com.thong.utils.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LeaveRepository extends JpaRepository<Leave, Integer> {

    // All leaves for a specific employee
    List<Leave> findByEmployeeIdOrderByCreatedAtDesc(Integer employeeId);

    // All leaves (admin view) — sorted by newest
    List<Leave> findAllByOrderByCreatedAtDesc();

    // Filter by status (admin: see all pending)
    List<Leave> findByStatusOrderByCreatedAtDesc(LeaveStatus status);

    // Check for overlapping leave dates — prevent double booking
    @Query("""
        SELECT COUNT(l) > 0 FROM Leave l
        WHERE l.employee.id = :employeeId
        AND l.status != 'REJECTED'
        AND l.startDate <= :endDate
        AND l.endDate >= :startDate
    """)
    boolean hasOverlappingLeave(
        @Param("employeeId") Integer employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // In EmployeeRepository.java — add this method
    @Query("SELECT e FROM Employee e WHERE e.user.email = :email")
    Optional<Employee> findByUserEmail(@Param("email") String email);


    @Query("""
    SELECT l FROM Leave l
    WHERE l.employee.id = :employeeId
    AND l.status = 'APPROVED'
    AND l.leaveType = 'UNPAID_LEAVE'
    AND l.startDate <= :monthEnd
    AND l.endDate >= :monthStart
""")
    List<Leave> findApprovedUnpaidLeavesInMonth(
            @Param("employeeId") Integer employeeId,
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd")   LocalDate monthEnd
    );

}