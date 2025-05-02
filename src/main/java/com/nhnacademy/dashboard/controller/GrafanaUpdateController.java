package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.dto.frontdto.update.UpdatePanelRequest;
import com.nhnacademy.dashboard.dto.frontdto.update.UpdateDashboardNameRequest;
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

    @PutMapping("/dashboard-name")
    @Operation(summary = "대시보드 이름 수정")
    public ResponseEntity<Void> updateDashboard(
            UpdateDashboardNameRequest updateDashboardNameRequest
    ){
        grafanaService.updateDashboardName(updateDashboardNameRequest);
        return ResponseEntity
                .ok().build();
    }

    @PutMapping("/dashboards/charts")
    @Operation(summary = "차트 쿼리 수정")
    public ResponseEntity<Void> updateChart(
            @RequestHeader("X-User-Id") String userId,
            UpdatePanelRequest updateRequest
    ){
        grafanaService.updateChart(userId, updateRequest);

        return ResponseEntity
                .ok().build();
    }
}
