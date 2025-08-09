package com.hrms.service.impl;

import com.hrms.DTOs.EmployeeDTO;
import com.hrms.DTOs.EmployeeRequestDTO;
import com.hrms.DTOs.EmployeeUpdateDTO;
import com.hrms.DTOs.EmployeePatchDTO;
import com.hrms.exception.HrmsException;
import com.hrms.exception.UnauthorizedException;
import com.hrms.model.Employee;
import com.hrms.model.Department;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.DepartmentRepository;
import com.hrms.service.EmployeeService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository empRepo;
    private final DepartmentRepository deptRepo;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(EmployeeRepository empRepo, DepartmentRepository deptRepo,
            PasswordEncoder passwordEncoder) {
        this.empRepo = empRepo;
        this.deptRepo = deptRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public EmployeeDTO addEmployee(EmployeeRequestDTO employeeRequestDTO) {
        Employee currentUser = getCurrentUser();
        validateEmployeeCreation(employeeRequestDTO, currentUser);

        Employee employee = convertToEntity(employeeRequestDTO);
        // If current user is Dept Head, ensure department assignment and manager are
        // within their dept
        if (currentUser.isDeptHead()) {
            if (employee.getDepartment() == null || !currentUser.getDepartment().equals(employee.getDepartment())) {
                throw new UnauthorizedException("Department head can create employees only in their department");
            }
            if (employee.getManager() == null) {
                throw new HrmsException("Manager is required when creating employee");
            }
            if (!employee.getManager().isCeo() && (employee.getManager().getDepartment() == null
                    || !employee.getManager().getDepartment().equals(currentUser.getDepartment()))) {
                throw new HrmsException("Manager must be from the same department or be the CEO");
            }
        }

        // When creating a department head, enforce single head per department and CEO
        // as manager
        if (employee.isDeptHead()) {
            Department department = Optional.ofNullable(employee.getDepartment())
                    .orElseThrow(() -> new HrmsException("Department head must belong to a department"));

            Department persistentDept = deptRepo.findById(department.getId())
                    .orElseThrow(() -> new HrmsException("Department not found"));

            if (persistentDept.getHead() != null) {
                throw new HrmsException("Department already has a head. Remove current head first.");
            }

            // Ensure department head reports to CEO
            Employee ceo = empRepo.findByIsCeoTrue().stream().findFirst()
                    .orElseThrow(() -> new HrmsException("CEO not found"));
            if (employee.getManager() == null) {
                employee.setManager(ceo);
            } else if (!employee.getManager().isCeo()) {
                throw new HrmsException("Department head must report to CEO");
            }
        }

        if (employee.getManager() != null) {
            validateManagerAssignment(employee, employee.getManager());
        }

        Employee savedEmployee = empRepo.save(employee);

        // If created as department head, set the department's head pointer
        if (savedEmployee.isDeptHead() && savedEmployee.getDepartment() != null) {
            Department deptToUpdate = deptRepo.findById(savedEmployee.getDepartment().getId())
                    .orElseThrow(() -> new HrmsException("Department not found"));
            if (deptToUpdate.getHead() != null && !deptToUpdate.getHead().getId().equals(savedEmployee.getId())) {
                throw new HrmsException("Department already has a head. Remove current head first.");
            }
            deptToUpdate.setHead(savedEmployee);
            deptRepo.save(deptToUpdate);
        }

        return convertToDTO(savedEmployee);
    }

    @Override
    public EmployeeDTO updateEmployee(Long id, EmployeeUpdateDTO employeeUpdateDTO) {
        Employee existingEmployee = empRepo.findById(id)
                .orElseThrow(() -> new HrmsException("Employee not found"));

        Employee currentUser = getCurrentUser();
        if (!canModifyEmployee(currentUser, existingEmployee)) {
            throw new UnauthorizedException(
                    "Only the employee's manager, department head or CEO can modify this employee");
        }

        if (currentUser.getId().equals(id)) {
            throw new UnauthorizedException(
                    "You cannot modify your own details.");
        }

        if (employeeUpdateDTO.getManagerId() != null || employeeUpdateDTO.getDepartmentId() != null) {

            throw new HrmsException("Cannot change manager or department via update. Use dedicated endpoints.");
        }




        validateEmployeeUpdate(existingEmployee, employeeUpdateDTO);

        updateEmployeeFields(existingEmployee, employeeUpdateDTO);
        Employee savedEmployee = empRepo.save(existingEmployee);

        return convertToDTO(savedEmployee);
    }

    @Override
    public EmployeeDTO patchEmployee(Long id, EmployeePatchDTO employeePatchDTO) {
        Employee existingEmployee = empRepo.findById(id)
                .orElseThrow(() -> new HrmsException("Employee not found"));


        Employee currentUser = getCurrentUser();
        if (!canModifyEmployee(currentUser, existingEmployee)) {
            throw new UnauthorizedException(
                    "Only the employee's manager, department head, or CEO can modify this employee");
        }

        if (currentUser.getId().equals(id)) {
            throw new UnauthorizedException("You cannot modify your own details.");
        }

       if (employeePatchDTO.getManagerId() != null || employeePatchDTO.getDepartmentId() != null) {

            throw new HrmsException("Cannot change manager or department via patch. Use dedicated endpoints.");
        }

        validateEmployeePatch(existingEmployee, employeePatchDTO);

        patchEmployeeFields(existingEmployee, employeePatchDTO);
        Employee savedEmployee = empRepo.save(existingEmployee);

        return convertToDTO(savedEmployee);
    }

    @Override
    public EmployeeDTO getEmployee(Long id) {
        Employee employee = empRepo.findById(id)
                .orElseThrow(() -> new HrmsException("Employee not found"));

        Employee currentUser = getCurrentUser();
        if (!currentUser.canViewEmployee(employee)) {
            throw new UnauthorizedException("You are not authorized to view this employee");
        }

        return convertToDTO(employee);
    }

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        Employee currentUser = getCurrentUser();
        if (!currentUser.isCeo()) {
            throw new UnauthorizedException("Only CEO can view all employees");
        }

        return empRepo.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDTO> getEmployeesByDept(Long deptId) {
        Employee currentUser = getCurrentUser();
        Department department = deptRepo.findById(deptId)
                .orElseThrow(() -> new HrmsException("Department not found"));

        boolean allowed = currentUser.isCeo()
                || (currentUser.isDeptHead() && department.equals(currentUser.getDepartment()));
        if (!allowed) {
            throw new UnauthorizedException("Only CEO or the department head can view employees in this department");
        }

        return empRepo.findByDepartmentId(deptId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeDTO moveEmployee(Long empId, Long deptId) {
        Employee currentUser = getCurrentUser();
        if (!currentUser.isCeo()) {
            throw new UnauthorizedException("Only CEO can move a department head");
        }

        Employee movingHead = empRepo.findById(empId)
                .orElseThrow(() -> new HrmsException("Employee not found"));

        if (!movingHead.isDeptHead()) {
            throw new HrmsException("This endpoint is only for moving department heads");
        }

        Department targetDepartment = deptRepo.findById(deptId)
                .orElseThrow(() -> new HrmsException("Target department not found"));

        Department sourceDepartment = movingHead.getDepartment();

        // If target department has a head, demote them and set their manager to the
        // moving head
        Employee existingTargetHead = targetDepartment.getHead();
        if (existingTargetHead != null) {
            existingTargetHead.setDeptHead(false);
            existingTargetHead.setManager(movingHead);
            empRepo.save(existingTargetHead);
        }

        // Break old head link first to satisfy unique head constraint
        if (sourceDepartment != null) {
            sourceDepartment.setHead(null);
            deptRepo.save(sourceDepartment);
            deptRepo.flush();
        }

        // Move head to target department and set CEO as manager
        Employee chiefExecutive = empRepo.findByIsCeoTrue().stream().findFirst()
                .orElseThrow(() -> new HrmsException("CEO not found"));
        movingHead.setDepartment(targetDepartment);
        movingHead.setManager(chiefExecutive);
        movingHead.setDeptHead(true);
        empRepo.save(movingHead);

        // Update department head pointers
        targetDepartment.setHead(movingHead);
        deptRepo.save(targetDepartment);
        deptRepo.flush();

        return convertToDTO(movingHead);
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = empRepo.findById(id)
                .orElseThrow(() -> new HrmsException("Employee not found"));

        Employee currentUser = getCurrentUser();
        if (currentUser.getId().equals(id)) {
            throw new UnauthorizedException("You cannot delete yourself. Only your superiors can do this.");
        }
        if (!canModifyEmployee(currentUser, employee)) {
            throw new UnauthorizedException(
                    "Only the employee's manager, department head or CEO can delete this employee");
        }

        if (employee.isCeo()) {
            throw new HrmsException("Cannot delete CEO");
        }

        if (employee.isDeptHead()) {
            throw new HrmsException("Cannot delete department head directly");
        }

        empRepo.delete(employee);
    }

    @Override
    public EmployeeDTO getCurrentUserProfile() {
        Employee currentUser = getCurrentUser();
        return convertToDTO(currentUser);
    }

    private void validateEmployeeCreation(EmployeeRequestDTO employeeRequestDTO, Employee currentUser) {
        if (empRepo.findByEmail(employeeRequestDTO.getEmail()).isPresent()) {
            throw new HrmsException("Email already exists");
        }

        if (employeeRequestDTO.isCeo()) {
            if (empRepo.findByIsCeoTrue().size() > 0) {
                throw new HrmsException("CEO already exists");
            }
        } else {
            if (employeeRequestDTO.getManagerId() == null) {
                throw new HrmsException("Non-CEO employees must have a manager");
            }
        }

        if (employeeRequestDTO.isDeptHead()) {
            if (employeeRequestDTO.getDepartmentId() == null) {
                throw new HrmsException("Department head must belong to a department");
            }
        }

        // Dept Head creation constraints
        if (currentUser.isDeptHead()) {
            if (employeeRequestDTO.getDepartmentId() == null
                    || !currentUser.getDepartment().getId().equals(employeeRequestDTO.getDepartmentId())) {
                throw new UnauthorizedException("Department head can create employees only in their department");
            }
        }
    }

    private void validateEmployeeUpdate(Employee existingEmployee, EmployeeUpdateDTO employeeUpdateDTO) {
        if (employeeUpdateDTO.getEmail() != null && !existingEmployee.getEmail().equals(employeeUpdateDTO.getEmail()) &&
                empRepo.findByEmail(employeeUpdateDTO.getEmail()).isPresent()) {
            throw new HrmsException("Email already exists");
        }
        if (employeeUpdateDTO.getIsCeo() != null) {
            throw new HrmsException("Cannot change CEO flag via update. Use dedicated operations.");
        }
        if (employeeUpdateDTO.getIsDeptHead() != null) {
            throw new HrmsException("Cannot change Department Head flag via update. Use dedicated operations.");
        }
    }

    private void validateEmployeePatch(Employee existingEmployee, EmployeePatchDTO employeePatchDTO) {
        if (employeePatchDTO.getEmail() != null && !existingEmployee.getEmail().equals(employeePatchDTO.getEmail()) &&
                empRepo.findByEmail(employeePatchDTO.getEmail()).isPresent()) {
            throw new HrmsException("Email already exists");
        }
        if (employeePatchDTO.getIsCeo() != null) {
            throw new HrmsException("Cannot change CEO flag via patch. Use dedicated operations.");
        }
        if (employeePatchDTO.getIsDeptHead() != null) {
            throw new HrmsException("Cannot change Department Head flag via patch. Use dedicated operations.");
        }
    }

    private Employee convertToEntity(EmployeeRequestDTO dto) {
        Employee employee = new Employee();
        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setRole(dto.getRole());
        if (dto.isCeo()) {
            if (empRepo.existsByIsCeoTrue()) {
                throw new HrmsException("CEO already exists");
            }
            employee.setDeptHead(false);
            dto.setDepartmentId(null);
            dto.setManagerId(null);
        } else {
            employee.setDeptHead(dto.isDeptHead());
        }
        employee.setCeo(dto.isCeo());

        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getDepartmentId() != null) {
            Department dept = deptRepo.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new HrmsException("Department not found"));
            employee.setDepartment(dept);
        }

        if (dto.getManagerId() != null) {
            Employee manager = empRepo.findById(dto.getManagerId())
                    .orElseThrow(() -> new HrmsException("Manager not found"));
            employee.setManager(manager);
        }
        return employee;
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
                .directReportsCount(empRepo.findByManagerId(employee.getId()).stream().count())
                .build();
    }

    private Employee getCurrentUser() {
        try {
            Employee employee = (Employee) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return empRepo.findById(employee.getId())
                    .orElseThrow(() -> new UnauthorizedException("User not found"));
        } catch (Exception e) {
            throw new UnauthorizedException("Unable to get current user");
        }
    }

    private void updateEmployeeFields(Employee employee, EmployeeUpdateDTO dto) {
        if (dto.getName() != null)
            employee.setName(dto.getName());
        if (dto.getEmail() != null)
            employee.setEmail(dto.getEmail());
        if (dto.getRole() != null)
            employee.setRole(dto.getRole());
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
    }

    private void patchEmployeeFields(Employee employee, EmployeePatchDTO dto) {
        if (dto.getName() != null)
            employee.setName(dto.getName());
        if (dto.getEmail() != null)
            employee.setEmail(dto.getEmail());
        if (dto.getRole() != null)
            employee.setRole(dto.getRole());
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
    }

    @Override
    public List<EmployeeDTO> getEmployeesByManager(Long managerId) {
        Employee manager = empRepo.findById(managerId)
                .orElseThrow(() -> new HrmsException("Manager not found"));

        Employee currentUser = getCurrentUser();
        if (!currentUser.canViewEmployee(manager)) {
            throw new UnauthorizedException("You are not authorized to view this manager's reports");
        }

        return empRepo.findByManagerId(managerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeDTO assignManager(Long empId, Long managerId) {
        Employee employee = empRepo.findById(empId)
                .orElseThrow(() -> new HrmsException("Employee not found"));

        Employee manager = empRepo.findById(managerId)
                .orElseThrow(() -> new HrmsException("Manager not found"));

        validateManagerAssignment(employee, manager);

        employee.setManager(manager);
        Employee savedEmployee = empRepo.save(employee);

        return convertToDTO(savedEmployee);
    }

    @Override
    public List<EmployeeDTO> getMyDirectReports() {
        Employee currentUser = getCurrentUser();
        return empRepo.findByManagerId(currentUser.getId()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private void validateManagerAssignment(Employee employee, Employee manager) {
        if (employee.isCeo()) {
            throw new HrmsException("CEO cannot have a manager");
        }

        if (employee.equals(manager)) {
            throw new HrmsException("Employee cannot be their own manager");
        }

        if (employee.getDepartment() != null && manager.getDepartment() != null &&
                !employee.getDepartment().equals(manager.getDepartment())) {
            throw new HrmsException("Employee and manager must be in same department");
        }

        if (manager.isCeo() && employee.getDepartment() != null) {
            long directReportsToCEO = empRepo.findByDepartmentId(employee.getDepartment().getId())
                    .stream()
                    .filter(e -> e.getManager() != null && e.getManager().isCeo())
                    .count();
            if (directReportsToCEO >= 1) {
                throw new HrmsException("Only one employee per department can report directly to CEO");
            }
        }
    }

    @Override
    public EmployeeDTO moveEmployeeWithManager(Long empId, Long newDeptId, Long newManagerId) {
        Employee currentUser = getCurrentUser();
        if (!currentUser.isCeo() && !currentUser.isDeptHead()) {
            throw new UnauthorizedException("Only CEO or Department Head can move employees");
        }

        Employee employee = empRepo.findById(empId)
                .orElseThrow(() -> new HrmsException("Employee not found"));
        if (employee.isCeo()) {
            throw new HrmsException("Cannot move CEO");
        }
        if (employee.isDeptHead()) {
            throw new HrmsException("Use Department Head move API for moving a department head");
        }

        if (currentUser.isDeptHead()) {
            if (employee.getDepartment() == null || !employee.getDepartment().equals(currentUser.getDepartment())) {
                throw new UnauthorizedException("Department head can move only employees from their department");
            }
        }

        Department newDept = deptRepo.findById(newDeptId)
                .orElseThrow(() -> new HrmsException("Department not found"));
        Employee newManager = empRepo.findById(newManagerId)
                .orElseThrow(() -> new HrmsException("Manager not found"));

        // Validate manager assignment against new department
        if (!newManager.isCeo()) {
            if (newManager.getDepartment() == null || !newManager.getDepartment().equals(newDept)) {
                throw new HrmsException("Manager must belong to the new department or be CEO");
            }
        } else {
            // CEO manager rule: only one direct report per department
            long directReportsToCEO = empRepo.findByDepartmentId(newDept.getId())
                    .stream()
                    .filter(e -> e.getManager() != null && e.getManager().isCeo())
                    .count();
            if (directReportsToCEO >= 1) {
                throw new HrmsException("Only one employee per department can report directly to CEO");
            }
        }

        employee.setDepartment(newDept);
        employee.setManager(newManager);
        Employee saved = empRepo.save(employee);
        return convertToDTO(saved);
    }

    @Override
    public EmployeeDTO moveDepartmentHead(Long headId, Long newDeptId, Long replacementHeadId) {
        Employee currentUser = getCurrentUser();
        if (!currentUser.isCeo()) {
            throw new UnauthorizedException("Only CEO can move a department head");
        }

        Employee movingHead = empRepo.findById(headId)
                .orElseThrow(() -> new HrmsException("Employee not found"));

        if (!movingHead.isDeptHead()) {
            throw new HrmsException("Specified employee is not a department head");
        }

        Department sourceDepartment = Optional.ofNullable(movingHead.getDepartment())
                .orElseThrow(() -> new HrmsException("Department head must belong to a department"));

        Department targetDepartment = deptRepo.findById(newDeptId)
                .orElseThrow(() -> new HrmsException("Target department not found"));

        Employee replacementHead = empRepo.findById(replacementHeadId)
                .orElseThrow(() -> new HrmsException("Replacement head not found"));

        if (replacementHead.isCeo()) {
            throw new HrmsException("CEO cannot be a department head");
        }
        if (replacementHead.isDeptHead()) {
            throw new HrmsException("Replacement is already a department head");
        }
        if (replacementHead.getDepartment() == null || !replacementHead.getDepartment().equals(sourceDepartment)) {
            throw new HrmsException("Replacement must belong to the source department");
        }

        Employee chiefExecutive = empRepo.findByIsCeoTrue().stream().findFirst()
                .orElseThrow(() -> new HrmsException("CEO not found"));

        // If target department already has a head, demote them and set their manager to
        // the incoming head
        Employee existingTargetHead = targetDepartment.getHead();
        if (existingTargetHead != null) {
            existingTargetHead.setDeptHead(false);
            existingTargetHead.setManager(movingHead);
            empRepo.save(existingTargetHead);
        }

        // Break old head link first to satisfy unique head constraint
        sourceDepartment.setHead(null);
        deptRepo.save(sourceDepartment);
        deptRepo.flush();

        // Assign replacement as head of source department (must report to CEO)
        replacementHead.setDeptHead(true);
        replacementHead.setManager(chiefExecutive);
        empRepo.save(replacementHead);
        sourceDepartment.setHead(replacementHead);
        deptRepo.save(sourceDepartment);
        deptRepo.flush();

        // Move the current head and assign as head of target department (manager is
        // CEO)
        movingHead.setDepartment(targetDepartment);
        movingHead.setDeptHead(true);
        movingHead.setManager(chiefExecutive);
        empRepo.save(movingHead);

        targetDepartment.setHead(movingHead);
        deptRepo.save(targetDepartment);
        deptRepo.flush();

        return convertToDTO(movingHead);
    }

    private boolean canModifyEmployee(Employee employee, Employee target) {
        if (employee.isCeo()) {
            return true;
        }
        if (target.getManager() != null && target.getManager().getId().equals(employee.getId())) {
            return true;
        }
        return employee.isDeptHead() && employee.getDepartment() != null
                && employee.getDepartment().equals(target.getDepartment());
    }

    private boolean ensureCeo() {
        Employee currentUser;
        try {
            currentUser = (Employee) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (Exception e) {
            throw new UnauthorizedException("Unable to get current user");
        }
        if (currentUser == null || !currentUser.isCeo()) {
            throw new UnauthorizedException("Only CEO can perform this operation");
        }
        return true;
    }
}