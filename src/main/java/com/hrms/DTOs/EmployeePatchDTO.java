package com.hrms.DTOs;

import com.hrms.model.Employee;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmployeePatchDTO {
    
    private String name;

    @Email(message = "Email should be valid")
    private String email;

    private String password;
    private Employee.UserRole role;
    private Long departmentId;
    private Long managerId;
    private Boolean isCeo;
    private Boolean isDeptHead;
} 