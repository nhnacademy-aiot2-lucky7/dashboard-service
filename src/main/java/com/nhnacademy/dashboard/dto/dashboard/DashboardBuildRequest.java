package com.nhnacademy.dashboard.dto.dashboard;

import com.nhnacademy.dashboard.dto.dashboard.json.GridPos;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Grafana 대시보드 생성 요청을 위한 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardBuildRequest {
    private String userId;
    private GridPos gridPos;
    private String type;
    private String dashboardTitle;
    private String panelTitle;
    private String fluxQuery;
    private Integer min;
    private Integer max;
}