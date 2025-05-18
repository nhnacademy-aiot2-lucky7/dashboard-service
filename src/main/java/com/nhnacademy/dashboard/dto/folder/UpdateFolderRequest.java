package com.nhnacademy.dashboard.dto.folder;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateFolderRequest {
    private String departmentId;
    private String newFolderName;
}
