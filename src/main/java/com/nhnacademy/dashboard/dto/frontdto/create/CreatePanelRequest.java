package com.nhnacademy.dashboard.dto.frontdto.create;

import com.nhnacademy.dashboard.dto.grafanadto.dashboarddto.GridPos;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CreatePanelRequest {
    private String folderUid;
    private String dashboardUid;
    private String dashboardTitle;
    private String title;
    private String measurement;
    private List<String> field;
    private GridPos gridPos;
    private String type;
    private String aggregation;
    private String time;

    public CreatePanelRequest(String folderUid, String dashboardUid, String dashboardTitle, String title, String measurement, List<String> field, String type, String aggregation, String time) {
        this.folderUid = folderUid;
        this.dashboardUid = dashboardUid;
        this.dashboardTitle = dashboardTitle;
        this.title = title;
        this.measurement = measurement;
        this.field = field;
        this.gridPos = new GridPos(12,8);
        this.type = type;
        this.aggregation = aggregation;
        this.time = time;
    }
}
