package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.dto.front_dto.update.UpdatePanelPriorityRequest;
import com.nhnacademy.dashboard.dto.front_dto.update.UpdatePanelRequest;
import com.nhnacademy.dashboard.dto.front_dto.update.UpdateDashboardNameRequest;
import com.nhnacademy.dashboard.service.UpdateService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GrafanaUpdateController {

    private final UpdateService updateService;

    @PutMapping("/dashboard-name")
    @Operation(summary = "대시보드 이름 수정")
    public ResponseEntity<Void> updateDashboard(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody UpdateDashboardNameRequest updateDashboardNameRequest
    ){
        updateService.updateDashboardName(userId, updateDashboardNameRequest);
        return ResponseEntity
                .ok().build();
    }

    @PutMapping("/dashboards/charts")
    @Operation(summary = "차트 쿼리 수정")
    public ResponseEntity<Void> updateChart(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody UpdatePanelRequest updateRequest
    ){
        updateService.updateChart(userId, updateRequest);

        return ResponseEntity
                .ok().build();
    }

    @PutMapping("/dashboards/priority")
    @Operation(summary = "차트 우선순위 수정")
    public ResponseEntity<Void> updatePriority(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody UpdatePanelPriorityRequest updatePriority
    ){
        updateService.updatePriority(userId, updatePriority);

        return ResponseEntity
                .ok().build();
    }
}
