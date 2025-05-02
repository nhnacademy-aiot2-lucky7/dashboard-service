package com.nhnacademy.dashboard.dto.frontdto.read;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReadFilterChartRequest {
    private String dashboardUid;
    private String filter;
}
