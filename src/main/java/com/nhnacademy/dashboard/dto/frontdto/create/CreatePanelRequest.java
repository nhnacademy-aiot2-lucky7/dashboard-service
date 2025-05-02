package com.nhnacademy.dashboard.dto.frontdto.create;

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
    private String type;
    private String aggregation;
    private String time;
}
