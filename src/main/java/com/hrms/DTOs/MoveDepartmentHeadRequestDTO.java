package com.hrms.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoveDepartmentHeadRequestDTO {
    @NotNull
    private Long newDepartmentId;
    @NotNull
    private Long replacementHeadEmployeeId;
} 