package com.nhnacademy.dashboard.dto.grafanadto;

import com.nhnacademy.dashboard.dto.grafanadto.dashboarddto.Dashboard;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JsonGrafanaDashboardRequest {

    private Dashboard dashboard;
    private String folderUid;
    private boolean overwrite;
}