package com.nhnacademy.dashboard.dto.folder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GrafanaUpdateFolderRequest {
    private String title;
    private int version;
}
