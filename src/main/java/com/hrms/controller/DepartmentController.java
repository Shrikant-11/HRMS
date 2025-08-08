package com.hrms.controller;

import com.hrms.DTOs.DepartmentDTO;
import com.hrms.DTOs.DepartmentRequestDTO;
import com.hrms.DTOs.DepartmentUpdateDTO;
import com.hrms.DTOs.DepartmentPatchDTO;
import com.hrms.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create New Department",
            description = "Creates a new department in the organization. Only ADMIN users can create departments."
    )
    public ResponseEntity<DepartmentDTO> createDepartment(
            @Parameter(description = "Department creation data", required = true)
            @Valid @RequestBody DepartmentRequestDTO departmentRequestDTO
    ) {
        DepartmentDTO createdDepartment = departmentService.createDepartment(departmentRequestDTO);
        return ResponseEntity.ok(createdDepartment);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update Department (Full Update)",
            description = "Performs a full update of a department. All fields must be provided. Only ADMIN users can update departments."
    )
    public ResponseEntity<DepartmentDTO> updateDepartment(
            @Parameter(description = "Department ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Department update data (all fields required)", required = true)
            @Valid @RequestBody DepartmentUpdateDTO departmentUpdateDTO
    ) {
        DepartmentDTO updatedDepartment = departmentService.updateDepartment(id, departmentUpdateDTO);
        return ResponseEntity.ok(updatedDepartment);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update Department (Partial Update)",
            description = "Performs a partial update of a department. Only provided fields will be updated. Only ADMIN users can update departments."
    )
    public ResponseEntity<DepartmentDTO> patchDepartment(
            @Parameter(description = "Department ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Department patch data (only fields to update)", required = true)
            @RequestBody DepartmentPatchDTO departmentPatchDTO
    ) {
        DepartmentDTO patchedDepartment = departmentService.patchDepartment(id, departmentPatchDTO);
        return ResponseEntity.ok(patchedDepartment);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get Department by ID",
            description = "Retrieves department details by ID. All authenticated users can view department information."
    )
    public ResponseEntity<DepartmentDTO> getDepartment(
            @Parameter(description = "Department ID", required = true, example = "1")
            @PathVariable Long id
    ) {
        DepartmentDTO department = departmentService.getDepartment(id);
        return ResponseEntity.ok(department);
    }

    @GetMapping
    @Operation(
            summary = "Get All Departments",
            description = "Retrieves all departments in the organization. All authenticated users can access this endpoint."
    )
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        List<DepartmentDTO> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete Department",
            description = "Deletes a department from the organization. Only ADMIN users can delete departments."
    )
    public ResponseEntity<Void> deleteDepartment(
            @Parameter(description = "Department ID", required = true, example = "1")
            @PathVariable Long id
    ) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok().build();
    }
}
