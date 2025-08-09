package com.hrms.service.impl;

import com.hrms.DTOs.DepartmentDTO;
import com.hrms.DTOs.DepartmentRequestDTO;
import com.hrms.DTOs.DepartmentUpdateDTO;
import com.hrms.DTOs.DepartmentPatchDTO;
import com.hrms.exception.HrmsException;
import com.hrms.exception.UnauthorizedException;
import com.hrms.model.Department;
import com.hrms.model.Employee;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.service.DepartmentService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository deptRepo;
    private final EmployeeRepository empRepo;

    public DepartmentServiceImpl(DepartmentRepository deptRepo, EmployeeRepository empRepo) {
        this.deptRepo = deptRepo;
        this.empRepo = empRepo;
    }

    @Override
    public DepartmentDTO createDepartment(DepartmentRequestDTO departmentRequestDTO) {
        ensureCeo();
        validateDepartmentCreation(departmentRequestDTO);

        Department department = convertToEntity(departmentRequestDTO);
        Department savedDepartment = deptRepo.save(department);

        return convertToDTO(savedDepartment);
    }

    @Override
    public DepartmentDTO updateDepartment(Long id, DepartmentUpdateDTO departmentUpdateDTO) {
        ensureCeo();
        Department existingDepartment = deptRepo.findById(id)
                .orElseThrow(() -> new HrmsException("Department not found"));

        validateDepartmentUpdate(existingDepartment, departmentUpdateDTO);

        updateDepartmentFields(existingDepartment, departmentUpdateDTO);
        Department savedDepartment = deptRepo.save(existingDepartment);

        return convertToDTO(savedDepartment);
    }

    @Override
    public DepartmentDTO patchDepartment(Long id, DepartmentPatchDTO departmentPatchDTO) {
        ensureCeo();
        Department existingDepartment = deptRepo.findById(id)
                .orElseThrow(() -> new HrmsException("Department not found"));

        patchDepartmentFields(existingDepartment, departmentPatchDTO);
        Department savedDepartment = deptRepo.save(existingDepartment);

        return convertToDTO(savedDepartment);
    }

    @Override
    public DepartmentDTO getDepartment(Long id) {
        Department department = deptRepo.findById(id)
                .orElseThrow(() -> new HrmsException("Department not found"));

        return convertToDTO(department);
    }

    @Override
    public List<DepartmentDTO> getAllDepartments() {
        return deptRepo.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

/*    @Override
    public void deleteDepartment(Long id) {
        ensureCeo();
        Department department = deptRepo.findById(id)
                .orElseThrow(() -> new HrmsException("Department not found"));

        if (department.hasHead()) {
            department.setHead(null);
        }

        if (!department.getEmployees().isEmpty()) {
            department.getEmployees().forEach(emp -> emp.setDepartment(null));
        }

        deptRepo.save(department);

        deptRepo.delete(department);
    }*/

    private void validateDepartmentCreation(DepartmentRequestDTO departmentRequestDTO) {
        if (deptRepo.findByName(departmentRequestDTO.getName()).isPresent()) {
            throw new HrmsException("Department with this name already exists");
        }
    }

    private void validateDepartmentUpdate(Department existingDepartment, DepartmentUpdateDTO departmentUpdateDTO) {
        if (!existingDepartment.getName().equals(departmentUpdateDTO.getName()) &&
            deptRepo.findByName(departmentUpdateDTO.getName()).isPresent()) {
            throw new HrmsException("Department with this name already exists");
        }
    }

    private void validateHeadAssignment(Department department, Employee employee) {
        if (employee.isCeo()) {
            throw new HrmsException("CEO cannot be a department head");
        }

        if (employee.getDepartment() != null && !employee.getDepartment().equals(department)) {
            throw new HrmsException("Employee must belong to the department to be its head");
        }

        if (department.hasHead()) {
            throw new HrmsException("Department already has a head. Remove current head first.");
        }

        if (employee.getManager() != null && !employee.getManager().isCeo()) {
            throw new HrmsException("Department head must report to CEO");
        }
    }

    private Department convertToEntity(DepartmentRequestDTO dto) {
        Department department = new Department();
        department.setName(dto.getName());
        department.setDescription(dto.getDescription());
        return department;
    }

    private DepartmentDTO convertToDTO(Department department) {
        return DepartmentDTO.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .headId(department.getHead() != null ? department.getHead().getId() : null)
                .headName(department.getHead() != null ? department.getHead().getName() : null)
                .employeeCount(department.getEmployees() != null ? (long) department.getEmployees().size() : 0L)
                .build();
    }

    private void updateDepartmentFields(Department department, DepartmentUpdateDTO dto) {
        if (dto.getName() != null) department.setName(dto.getName());
        if (dto.getDescription() != null) department.setDescription(dto.getDescription());
    }

    private void patchDepartmentFields(Department department, DepartmentPatchDTO dto) {
        if (dto.getName() != null) department.setName(dto.getName());
        if (dto.getDescription() != null) department.setDescription(dto.getDescription());
    }

    private void ensureCeo() {
        Employee currentUser;
        try {
            currentUser = (Employee) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (Exception e) {
            throw new UnauthorizedException("Unable to get current user");
        }
        if (currentUser == null || !currentUser.isCeo()) {
            throw new UnauthorizedException("Only CEO can perform this operation");
        }
    }
}