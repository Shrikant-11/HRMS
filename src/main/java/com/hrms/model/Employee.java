package com.hrms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.util.List;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Role is required")
    private UserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Employee> directReports;

    private boolean isCeo;
    private boolean isDeptHead;

    public enum UserRole {
        ADMIN, EMPLOYEE
    }

    public boolean isAdmin() {
        return UserRole.ADMIN.equals(this.role);
    }

    public boolean isEmployee() {
        return UserRole.EMPLOYEE.equals(this.role);
    }

    public boolean canManageEmployees() {
        return isCeo() || isDeptHead();
    }

    public boolean canViewEmployee(Employee target) {
        return isCeo() || this.equals(target) ||
               (isDeptHead() && this.getDepartment() != null && this.getDepartment().equals(target.getDepartment()));
    }
}