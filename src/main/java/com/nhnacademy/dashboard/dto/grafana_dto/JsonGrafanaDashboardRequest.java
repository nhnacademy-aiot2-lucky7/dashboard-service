package com.nhnacademy.dashboard.dto.grafana_dto;

import com.nhnacademy.dashboard.dto.grafana_dto.dashboard_dto.Dashboard;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JsonGrafanaDashboardRequest {

    private Dashboard dashboard;
    private String folderUid;
    private boolean overwrite;
}