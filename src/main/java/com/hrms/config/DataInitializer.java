package com.hrms.config;

import com.hrms.model.Department;
import com.hrms.model.Employee;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(EmployeeRepository employeeRepository, 
                         DepartmentRepository departmentRepository,
                         PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        Employee ceo = new Employee();
        ceo.setName("John CEO");
        ceo.setEmail("ceo@company.com");
        ceo.setPassword(passwordEncoder.encode("password"));
        ceo.setRole(Employee.UserRole.ADMIN);
        ceo.setCeo(true);
        ceo.setDeptHead(false);
        ceo.setDepartment(null);
        ceo.setManager(null);
        employeeRepository.save(ceo);

        Department engineering = new Department();
        engineering.setName("Engineering");
        engineering.setDescription("Software Development Team");
        departmentRepository.save(engineering);

        Department marketing = new Department();
        marketing.setName("Marketing");
        marketing.setDescription("Marketing and Sales Team");
        departmentRepository.save(marketing);

        Department hr = new Department();
        hr.setName("Human Resources");
        hr.setDescription("HR and Recruitment Team");
        departmentRepository.save(hr);

        Employee engHead = new Employee();
        engHead.setName("Alice Engineering");
        engHead.setEmail("eng.head@company.com");
        engHead.setPassword(passwordEncoder.encode("password"));
        engHead.setRole(Employee.UserRole.ADMIN);
        engHead.setCeo(false);
        engHead.setDeptHead(true);
        engHead.setDepartment(engineering);
        engHead.setManager(ceo);
        employeeRepository.save(engHead);

        Employee marketingHead = new Employee();
        marketingHead.setName("Bob Marketing");
        marketingHead.setEmail("marketing.head@company.com");
        marketingHead.setPassword(passwordEncoder.encode("password"));
        marketingHead.setRole(Employee.UserRole.ADMIN);
        marketingHead.setCeo(false);
        marketingHead.setDeptHead(true);
        marketingHead.setDepartment(marketing);
        marketingHead.setManager(ceo);
        employeeRepository.save(marketingHead);

        Employee hrHead = new Employee();
        hrHead.setName("Carol HR");
        hrHead.setEmail("hr.head@company.com");
        hrHead.setPassword(passwordEncoder.encode("password"));
        hrHead.setRole(Employee.UserRole.ADMIN);
        hrHead.setCeo(false);
        hrHead.setDeptHead(true);
        hrHead.setDepartment(hr);
        hrHead.setManager(ceo);
        employeeRepository.save(hrHead);

        engineering.setHead(engHead);
        marketing.setHead(marketingHead);
        hr.setHead(hrHead);
        departmentRepository.save(engineering);
        departmentRepository.save(marketing);
        departmentRepository.save(hr);

        Employee dev1 = new Employee();
        dev1.setName("David Developer");
        dev1.setEmail("dev1@company.com");
        dev1.setPassword(passwordEncoder.encode("password"));
        dev1.setRole(Employee.UserRole.EMPLOYEE);
        dev1.setCeo(false);
        dev1.setDeptHead(false);
        dev1.setDepartment(engineering);
        dev1.setManager(engHead);
        employeeRepository.save(dev1);

        Employee dev2 = new Employee();
        dev2.setName("Eve Engineer");
        dev2.setEmail("dev2@company.com");
        dev2.setPassword(passwordEncoder.encode("password"));
        dev2.setRole(Employee.UserRole.EMPLOYEE);
        dev2.setCeo(false);
        dev2.setDeptHead(false);
        dev2.setDepartment(engineering);
        dev2.setManager(engHead);
        employeeRepository.save(dev2);

        Employee marketer1 = new Employee();
        marketer1.setName("Frank Marketer");
        marketer1.setEmail("marketer1@company.com");
        marketer1.setPassword(passwordEncoder.encode("password"));
        marketer1.setRole(Employee.UserRole.EMPLOYEE);
        marketer1.setCeo(false);
        marketer1.setDeptHead(false);
        marketer1.setDepartment(marketing);
        marketer1.setManager(marketingHead);
        employeeRepository.save(marketer1);

        Employee hrStaff = new Employee();
        hrStaff.setName("Grace HR");
        hrStaff.setEmail("hr.staff@company.com");
        hrStaff.setPassword(passwordEncoder.encode("password"));
        hrStaff.setRole(Employee.UserRole.EMPLOYEE);
        hrStaff.setCeo(false);
        hrStaff.setDeptHead(false);
        hrStaff.setDepartment(hr);
        hrStaff.setManager(hrHead);
        employeeRepository.save(hrStaff);

        System.out.println("Sample data initialized successfully!");
        System.out.println("CEO: ceo@company.com / password");
        System.out.println("Engineering Head: eng.head@company.com / password");
        System.out.println("Marketing Head: marketing.head@company.com / password");
        System.out.println("HR Head: hr.head@company.com / password");
        System.out.println("Developer: dev1@company.com / password");
        System.out.println("Marketer: marketer1@company.com / password");
        System.out.println("HR Staff: hr.staff@company.com / password");
    }
} 