package com.nhnacademy.dashboard.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ChartCreateRequest {
    private final String folderTitle;
    private final String dashboardTitle;
    private final String title;
    private final String measurement;
    private final List<String> field;
    private final String type;
    private final String aggregation;
    private final String time;
}
