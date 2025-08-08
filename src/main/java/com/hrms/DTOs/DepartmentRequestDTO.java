package com.hrms.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DepartmentRequestDTO {

    @NotBlank(message = "Department name is required")
    private String name;

    private String description;
}
