package com.hrms.controller;

import com.hrms.DTOs.AuthDTO;
import com.hrms.DTOs.EmployeeDTO;
import com.hrms.DTOs.EmployeeRequestDTO;
import com.hrms.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "User Login",
            description = "Authenticates a user with email and password, returns JWT token for API access"
    )
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody AuthDTO authDTO) {
        String token = authService.login(authDTO);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("message", "Login successful");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register New Employee",
            description = "Creates a new employee account. Only accessible by ADMIN users. Validates business rules for employee creation."
    )
    public ResponseEntity<EmployeeDTO> register(@Valid @RequestBody EmployeeRequestDTO employeeRequestDTO) {
        EmployeeDTO registeredEmployee = authService.register(employeeRequestDTO);
        return ResponseEntity.ok(registeredEmployee);
    }
}
