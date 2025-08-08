package com.hrms.service;

import com.hrms.DTOs.AuthDTO;
import com.hrms.DTOs.EmployeeDTO;
import com.hrms.DTOs.EmployeeRequestDTO;

public interface AuthService {
    String login(AuthDTO authDTO);
    EmployeeDTO register(EmployeeRequestDTO employeeRequestDTO);
} 