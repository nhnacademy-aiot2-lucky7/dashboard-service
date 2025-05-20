package com.nhnacademy.dashboard.dto.folder;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateFolderDepartmentIdRequest {

    @NotBlank(message = "departmentId 는 비어있을 수 없습니다.")
    private String departmentId;
}
