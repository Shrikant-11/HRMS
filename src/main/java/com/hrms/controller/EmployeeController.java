package com.hrms.controller;

import com.hrms.DTOs.EmployeeDTO;
import com.hrms.DTOs.EmployeeRequestDTO;
import com.hrms.DTOs.EmployeeUpdateDTO;
import com.hrms.DTOs.EmployeePatchDTO;
import com.hrms.DTOs.MoveDepartmentHeadRequestDTO;
import com.hrms.DTOs.MoveEmployeeRequestDTO;
import com.hrms.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee Management", description = "Employee CRUD operations and organizational management")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create New Employee",
            description = "Creates a new employee in the organization. Only ADMIN users can create employees. Validates business rules for employee creation.")
    public ResponseEntity<EmployeeDTO> createEmployee(
            @Parameter(
                    description = "Employee creation data",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"name\": \"John Doe\", \"email\": \"john@company.com\", \"password\": \"password\", \"role\": \"EMPLOYEE\", \"departmentId\": 1}"
                            )
                    )
            )
            @Valid @RequestBody EmployeeRequestDTO employeeRequestDTO
    ) {
        EmployeeDTO createdEmployee = employeeService.addEmployee(employeeRequestDTO);
        return ResponseEntity.ok(createdEmployee);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Employee (Full Update)",
            description = "Performs a full update of an employee. All fields must be provided. Only ADMIN users can update employees.")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @Parameter(description = "Employee ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(
                    description = "Employee update data (all fields required)",
                    required = true
            )
            @Valid @RequestBody EmployeeUpdateDTO employeeUpdateDTO
    ) {
        EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, employeeUpdateDTO);
        return ResponseEntity.ok(updatedEmployee);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Employee (Partial Update)",
            description = "Performs a partial update of an employee. Only provided fields will be updated. Only ADMIN users can update employees.")
    public ResponseEntity<EmployeeDTO> patchEmployee(
            @Parameter(description = "Employee ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Employee patch data (only fields to update)", required = true)
            @RequestBody EmployeePatchDTO employeePatchDTO) {
        EmployeeDTO patchedEmployee = employeeService.patchEmployee(id, employeePatchDTO);
        return ResponseEntity.ok(patchedEmployee);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Employee by ID",
            description = "Retrieves employee details by ID. Users can view their own profile or employees they manage.")
    public ResponseEntity<EmployeeDTO> getEmployee(
            @Parameter(description = "Employee ID", required = true, example = "1")
            @PathVariable Long id) {
        EmployeeDTO employee = employeeService.getEmployee(id);
        return ResponseEntity.ok(employee);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation( summary = "Get All Employees",
            description = "Retrieves all employees in the organization. Only CEO can access this endpoint.")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        List<EmployeeDTO> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/department/{deptId}")
    @Operation(summary = "Get Employees by Department",
            description = "Retrieves all employees in a specific department. Only CEO or the department head of that department can access.")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByDepartment(
            @Parameter(description = "Department ID", required = true, example = "1")
            @PathVariable Long deptId
    ) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByDept(deptId);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/manager/{managerId}")
    @Operation(summary = "Get Employees by Manager",
            description = "Retrieves all employees reporting to a specific manager. Users can view reports of managers they manage.")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByManager(@Parameter(description = "Manager ID", required = true, example = "2")
                                                                   @PathVariable Long managerId) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByManager(managerId);
        return ResponseEntity.ok(employees);
    }

    @PutMapping("/{empId}/manager/{managerId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign Manager to Employee",
            description = "Assigns a manager to an employee. Only ADMIN users can perform this operation. Validates business rules for manager assignment.")
    public ResponseEntity<EmployeeDTO> assignManager(@Parameter(description = "Employee ID", required = true, example = "3")
            @PathVariable Long empId,
            @Parameter(description = "Manager ID", required = true, example = "2")
            @PathVariable Long managerId) {
        EmployeeDTO updatedEmployee = employeeService.assignManager(empId, managerId);
        return ResponseEntity.ok(updatedEmployee);
    }

    @GetMapping("/reportings")
    @Operation(summary = "Get My Direct Reportings",
            description = "Retrieves all employees directly reporting to the current user. Employees can view their own direct reports.")
    public ResponseEntity<List<EmployeeDTO>> getMyDirectReports() {
        List<EmployeeDTO> reports = employeeService.getMyDirectReports();
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/profile")
    @Operation(summary = "Get Current User Profile",
            description = "Retrieves the profile of the currently authenticated user.")
    public ResponseEntity<EmployeeDTO> getCurrentUserProfile() {
        EmployeeDTO profile = employeeService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
    }

    // New: Move normal employee (CEO or Dept Head). Includes department and manager update.
    @PutMapping("/{empId}/move")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Move Employee",
            description = "Moves a non-head employee to another department and assigns a manager in that department. Allowed for CEO and Department Head (only for their department's employees).")
    public ResponseEntity<EmployeeDTO> moveEmployeeWithManager(
            @Parameter(description = "Employee ID", required = true, example = "3")
            @PathVariable Long empId,
            @Valid @RequestBody MoveEmployeeRequestDTO request
    ) {
        EmployeeDTO moved = employeeService.moveEmployeeWithManager(empId, request.getDepartmentId(), request.getManagerId());
        return ResponseEntity.ok(moved);
    }

    // New: Move Department Head (CEO only). Also assigns replacement head in source department.
    @PutMapping("/department-heads/{headId}/move")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Move Department Head",
            description = "Moves a department head to another department as head and assigns a replacement head for the source department. Allowed for CEO only.")
    public ResponseEntity<EmployeeDTO> moveDepartmentHead(
            @Parameter(description = "Head Employee ID", required = true, example = "2")
            @PathVariable Long headId,
            @Valid @RequestBody MoveDepartmentHeadRequestDTO request
    ) {
        EmployeeDTO movedHead = employeeService.moveDepartmentHead(headId, request.getNewDepartmentId(), request.getReplacementHeadEmployeeId());
        return ResponseEntity.ok(movedHead);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete Employee",
            description = "Deletes an employee from the organization. Only ADMIN users can delete employees.")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok().build();
    }
}