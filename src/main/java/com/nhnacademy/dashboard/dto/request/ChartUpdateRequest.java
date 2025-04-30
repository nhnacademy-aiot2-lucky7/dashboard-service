package com.nhnacademy.dashboard.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ChartUpdateRequest {
    private final String folderTitle;
    private final String dashboardTitle;
    private final String chartTitle;
    private final String title;
    private final String measurement;
    private final List<String> field;
    private final String type;
    private final String aggregation;
    private final String time;
}
