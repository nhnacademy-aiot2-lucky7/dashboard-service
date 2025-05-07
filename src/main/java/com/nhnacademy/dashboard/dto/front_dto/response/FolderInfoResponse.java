package com.nhnacademy.dashboard.dto.front_dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FolderInfoResponse {
    private int folderId;
    private String folderUid;
    private String folderTitle;
}