package com.hrms.service;

import com.hrms.DTOs.DepartmentDTO;
import com.hrms.DTOs.DepartmentRequestDTO;
import com.hrms.DTOs.DepartmentUpdateDTO;
import com.hrms.DTOs.DepartmentPatchDTO;
import java.util.List;

public interface DepartmentService {
    DepartmentDTO createDepartment(DepartmentRequestDTO departmentRequestDTO);
    DepartmentDTO updateDepartment(Long id, DepartmentUpdateDTO departmentUpdateDTO);
    DepartmentDTO patchDepartment(Long id, DepartmentPatchDTO departmentPatchDTO);
    DepartmentDTO getDepartment(Long id);
    List<DepartmentDTO> getAllDepartments();
    //void deleteDepartment(Long id);
}