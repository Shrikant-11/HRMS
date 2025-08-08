package com.hrms.service.impl;

import com.hrms.DTOs.AuthDTO;
import com.hrms.DTOs.EmployeeDTO;
import com.hrms.DTOs.EmployeeRequestDTO;
import com.hrms.exception.HrmsException;
import com.hrms.exception.UnauthorizedException;
import com.hrms.model.Employee;
import com.hrms.repository.EmployeeRepository;
import com.hrms.security.JwtUtil;
import com.hrms.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public String login(AuthDTO authDTO) {
        Employee employee = employeeRepository.findByEmail(authDTO.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(authDTO.getPassword(), employee.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return jwtUtil.generateToken(employee.getEmail(), employee.getRole().name());
    }

    @Override
    public EmployeeDTO register(EmployeeRequestDTO employeeRequestDTO) {
        if (employeeRepository.findByEmail(employeeRequestDTO.getEmail()).isPresent()) {
            throw new HrmsException("Email already exists");
        }
        if (employeeRepository.existsByIsCeoTrue()) {
            throw new HrmsException("CEO is already exists");
        }

        Employee employee = new Employee();
        employee.setName(employeeRequestDTO.getName());
        employee.setEmail(employeeRequestDTO.getEmail());
        employee.setPassword(passwordEncoder.encode(employeeRequestDTO.getPassword()));
        employee.setRole(employeeRequestDTO.getRole());
        employee.setCeo(employeeRequestDTO.isCeo());
        employee.setDeptHead(employeeRequestDTO.isDeptHead());

        Employee savedEmployee = employeeRepository.save(employee);
        return convertToDTO(savedEmployee);
    }

    private EmployeeDTO convertToDTO(Employee employee) {
        return EmployeeDTO.builder()
                .id(employee.getId())
                .name(employee.getName())
                .email(employee.getEmail())
                .role(employee.getRole())
                .isCeo(employee.isCeo())
                .isDeptHead(employee.isDeptHead())
                .departmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .managerId(employee.getManager() != null ? employee.getManager().getId() : null)
                .managerName(employee.getManager() != null ? employee.getManager().getName() : null)
                .directReportsCount(
                        employee.getDirectReports() != null ? (long) employee.getDirectReports().size() : 0L)
                .build();
    }
}