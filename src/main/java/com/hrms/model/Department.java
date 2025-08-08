package com.hrms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.util.List;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Department name is required")
    @Column(unique = true)
    private String name;

    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_id")
    private Employee head;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<Employee> employees;

    public boolean hasHead() {
        return head != null;
    }

    public boolean isHead(Employee employee) {
        return head != null && head.equals(employee);
    }
}