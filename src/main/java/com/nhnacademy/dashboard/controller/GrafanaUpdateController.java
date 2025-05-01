package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.dto.request.ChartUpdateRequest;
import com.nhnacademy.dashboard.dto.response.GrafanaResponse;
import com.nhnacademy.dashboard.service.impl.GrafanaServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GrafanaUpdateController {

    private final GrafanaServiceImpl grafanaService;

    @PutMapping("/dashboardName")
    @Operation(summary = "대시보드 이름 수정")
    public ResponseEntity<GrafanaResponse> updateDashboard(
            @RequestBody String dashboardUid,
            @RequestBody String title
    ){
        GrafanaResponse response = grafanaService.updateDashboardName(dashboardUid, title);
        return ResponseEntity
                .ok(response);
    }

    @PutMapping("/dashboards/charts")
    @Operation(summary = "차트 쿼리 수정")
    public ResponseEntity<GrafanaResponse> updateChart(
            ChartUpdateRequest updateRequest
    ){
        GrafanaResponse response = grafanaService.updateChart(updateRequest);

        return ResponseEntity
                .ok(response);
    }
}
