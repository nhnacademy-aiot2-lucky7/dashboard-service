package com.nhnacademy.dashboard.dto.frontdto.update;

import com.nhnacademy.dashboard.dto.grafanadto.dashboarddto.GridPos;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UpdatePanelRequest {
    private String dashboardTitle;
    private String dashboardUid;
    private String chartTitle;
    private String chartNewTitle;
    private String measurement;
    private List<String> field;
    private GridPos gridPos;
    private String graphType;
    private String aggregation;
    private String time;

    public UpdatePanelRequest(String dashboardTitle, String dashboardUid, String chartTitle, String chartNewTitle, String measurement, List<String> field, String graphType, String aggregation, String time) {
        this.dashboardTitle = dashboardTitle;
        this.dashboardUid = dashboardUid;
        this.chartTitle = chartTitle;
        this.chartNewTitle = chartNewTitle;
        this.measurement = measurement;
        this.field = field;
        this.gridPos = new GridPos(12,8);
        this.graphType = graphType;
        this.aggregation = aggregation;
        this.time = time;
    }
}
