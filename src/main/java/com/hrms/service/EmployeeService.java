package com.hrms.service;

import com.hrms.DTOs.EmployeeDTO;
import com.hrms.DTOs.EmployeeRequestDTO;
import com.hrms.DTOs.EmployeeUpdateDTO;
import com.hrms.DTOs.EmployeePatchDTO;
import java.util.List;

public interface EmployeeService {
    EmployeeDTO addEmployee(EmployeeRequestDTO employeeRequestDTO);
    EmployeeDTO updateEmployee(Long id, EmployeeUpdateDTO employeeUpdateDTO);
    EmployeeDTO patchEmployee(Long id, EmployeePatchDTO employeePatchDTO);
    EmployeeDTO getEmployee(Long id);
    List<EmployeeDTO> getAllEmployees();
    List<EmployeeDTO> getEmployeesByDept(Long deptId);
    List<EmployeeDTO> getEmployeesByManager(Long managerId);
    EmployeeDTO moveEmployee(Long empId, Long deptId);
    EmployeeDTO assignManager(Long empId, Long managerId);
    void deleteEmployee(Long id);
    EmployeeDTO getCurrentUserProfile();
    List<EmployeeDTO> getMyDirectReports();

    // New APIs
    EmployeeDTO moveEmployeeWithManager(Long empId, Long newDeptId, Long newManagerId);
    EmployeeDTO moveDepartmentHead(Long headId, Long newDeptId, Long replacementHeadId);

}