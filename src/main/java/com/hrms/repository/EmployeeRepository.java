package com.hrms.repository;

import com.hrms.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByDepartmentId(Long deptId);
    List<Employee> findByManagerId(Long managerId);
    Optional<Employee> findByEmail(String email);
    boolean existsByIsCeoTrue();
    List<Employee> findByRole(Employee.UserRole role);
    List<Employee> findByIsCeoTrue();
    List<Employee> findByIsDeptHeadTrue();

}