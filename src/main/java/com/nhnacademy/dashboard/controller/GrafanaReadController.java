package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.dto.frontdto.read.ReadChartRequest;
import com.nhnacademy.dashboard.dto.frontdto.read.ReadFilterChartRequest;
import com.nhnacademy.dashboard.dto.frontdto.response.DashboardInfoResponse;
import com.nhnacademy.dashboard.dto.frontdto.response.FolderInfoResponse;
import com.nhnacademy.dashboard.dto.frontdto.response.IframePanelResponse;
import com.nhnacademy.dashboard.exception.NotFoundException;
import com.nhnacademy.dashboard.service.impl.GrafanaServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GrafanaReadController {

    private final GrafanaServiceImpl grafanaService;
    /**
     * 모든 폴더를 조회하는 API.
     *
     * @return 모든 폴더 목록
     * @throws NotFoundException 폴더가 없을 경우 예외 발생
     */
    @GetMapping("/folders")
    @Operation(summary = "모든 폴더 조회")
    public ResponseEntity<List<FolderInfoResponse>> getFolders(){

        List<FolderInfoResponse> result = grafanaService.getAllFolders();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/dashboardsName")
    @Operation(summary ="모든 대시보드 이름 조회")
    public ResponseEntity<List<String>> getDashboardName(@RequestHeader("X-User-Id") String userId) {
        List<DashboardInfoResponse> dashboards = grafanaService.getDashboard(userId);

        List<String> result = dashboards.stream()
                .map(DashboardInfoResponse::getDashboardTitle)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/dashboards")
    @Operation(summary ="모든 대시보드 조회")
    public ResponseEntity<List<DashboardInfoResponse>> getAllDashboard(@RequestHeader("X-User-Id") String userId) {

        List<DashboardInfoResponse> result = grafanaService.getDashboard(userId);
        return ResponseEntity.ok(result);
    }


    @GetMapping(value = "/dashboards/charts")
    @Operation(summary = "차트 조회")
    public ResponseEntity<List<IframePanelResponse>> getChartByName(
            @RequestBody ReadChartRequest readChartRequest) {

        List<IframePanelResponse> result = grafanaService.getChart(readChartRequest);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/dashboards/filtered-chart")
    @Operation(summary ="메인페이지 on/off 필터 조회")
    public ResponseEntity<List<IframePanelResponse>> getDashboardCharts(
            @RequestBody ReadFilterChartRequest readFilterChartRequest
    ) {
        Map<String, String> filterMap = grafanaService.parseFilter(readFilterChartRequest.getFilter());
        List<IframePanelResponse> charts  = grafanaService.getFilterCharts(readFilterChartRequest.getDashboardUid(), filterMap);
        return ResponseEntity.ok(charts);
    }
}
