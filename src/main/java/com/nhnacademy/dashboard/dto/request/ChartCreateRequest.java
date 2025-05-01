package com.nhnacademy.dashboard.dto.request;

import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class ChartCreateRequest {
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
