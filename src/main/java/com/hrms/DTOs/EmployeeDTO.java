package com.hrms.DTOs;

import com.hrms.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmployeeDTO {

    private Long id;
    private String name;
    private String email;
    private Employee.UserRole role;
    private Long departmentId;
    private String departmentName;
    private Long managerId;
    private String managerName;
    private boolean isCeo;
    private boolean isDeptHead;
    private Long directReportsCount;
}
