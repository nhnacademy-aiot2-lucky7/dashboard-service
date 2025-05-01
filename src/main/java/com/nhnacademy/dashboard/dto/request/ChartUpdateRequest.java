package com.nhnacademy.dashboard.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChartUpdateRequest {
    private String folderUid;
    private String dashboardTitle;
    private String dashboardUid;
    private String chartTitle;
    private String title;
    private String measurement;
    private List<String> field;
    private String type;
    private String aggregation;
    private String time;
}
