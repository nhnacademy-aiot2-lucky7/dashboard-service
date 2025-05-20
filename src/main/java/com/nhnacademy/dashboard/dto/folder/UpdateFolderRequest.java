package com.nhnacademy.dashboard.dto.folder;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateFolderRequest {

    @NotBlank(message = "departmentId 는 비어있을 수 없습니다.")
    private String departmentId;

    @NotBlank(message = "newFolderName 는 비어있을 수 없습니다.")
    private String newFolderName;
}
