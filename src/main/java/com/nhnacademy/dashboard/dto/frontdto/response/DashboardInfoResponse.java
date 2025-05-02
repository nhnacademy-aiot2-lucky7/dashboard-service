package com.nhnacademy.dashboard.dto.frontdto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardInfoResponse {
    private int dashboardId;
    private String dashboardTitle;
    private String dashboardUid;
    private String folderUid;
    private int folderId;
}