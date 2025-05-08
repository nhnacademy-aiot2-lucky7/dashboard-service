package com.nhnacademy.dashboard.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InfoDashboardResponse {
    private int dashboardId;
    private String dashboardTitle;
    private String dashboardUid;
    private String folderUid;
    private int folderId;
}