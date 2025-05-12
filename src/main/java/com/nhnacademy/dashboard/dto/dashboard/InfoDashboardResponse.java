package com.nhnacademy.dashboard.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InfoDashboardResponse {

    @JsonProperty("id")
    private int dashboardId;

    @JsonProperty("title")
    private String dashboardTitle;

    @JsonProperty("uid")
    private String dashboardUid;

    @JsonProperty("folderUid")
    private String folderUid;

    @JsonProperty("folderId")
    private int folderId;
}