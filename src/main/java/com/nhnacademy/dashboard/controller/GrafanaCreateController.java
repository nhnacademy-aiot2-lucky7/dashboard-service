package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.dto.request.ChartCreateRequest;
import com.nhnacademy.dashboard.dto.response.GrafanaResponse;
import com.nhnacademy.dashboard.service.impl.GrafanaServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GrafanaCreateController {

    private final GrafanaServiceImpl grafanaService;

    @PostMapping("/dashboards")
    @Operation(summary = "새로운 대시보드 추가")
    public ResponseEntity<Void> createDashboard(
            @RequestHeader String id,
            @RequestBody String dashboardTitle) {

        grafanaService.createDashboard(id, dashboardTitle);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @PostMapping("/charts")
    @Operation(summary = "새로운 차트 추가")
    public ResponseEntity<GrafanaResponse> createChart(
            @RequestHeader String id,
            @RequestBody ChartCreateRequest chartCreateRequest
    ) {

        GrafanaResponse response =grafanaService.createChart(id, chartCreateRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
