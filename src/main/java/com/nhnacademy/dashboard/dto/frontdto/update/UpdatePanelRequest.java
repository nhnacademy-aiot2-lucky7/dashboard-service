package com.nhnacademy.dashboard.dto.frontdto.update;

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
    private String graphType;
    private String aggregation;
    private String time;
}
