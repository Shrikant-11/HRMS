package com.hrms.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DepartmentDTO {

    private Long id;

    private String name;

    private String description;

    private Long headId;

    private String headName;

    private Long employeeCount;
}
