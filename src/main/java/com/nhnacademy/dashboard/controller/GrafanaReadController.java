package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.dto.response.IdAndUidResponse;
import com.nhnacademy.dashboard.dto.response.FolderInfoResponse;
import com.nhnacademy.dashboard.dto.response.IframeResponse;
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
    public ResponseEntity<List<String>> getDashboardName(@RequestHeader String id) {
        List<IdAndUidResponse> dashboards = grafanaService.getDashboard(id);

        List<String> result = dashboards.stream()
                .map(IdAndUidResponse::getTitle)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/dashboards")
    @Operation(summary ="모든 대시보드 조회")
    public ResponseEntity<List<IdAndUidResponse>> getAllDashboard(@RequestHeader String id) {

        List<IdAndUidResponse> result = grafanaService.getDashboard(id);
        return ResponseEntity.ok(result);
    }


    @GetMapping(value = "/dashboards/charts")
    @Operation(summary = "차트 조회")
    public ResponseEntity<List<IframeResponse>> getChartByName(
            @RequestBody String dashboardUid) {

        List<IframeResponse> result = grafanaService.getChart(dashboardUid);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/dashboards/filtered-chart")
    @Operation(summary ="메인페이지 on/off 필터 조회")
    public ResponseEntity<List<IframeResponse>> getDashboardCharts(
            @RequestBody String dashboardUid,
            @RequestParam(name = "filter", required = false) String filter
    ) {
        Map<String, String> filterMap = grafanaService.parseFilter(filter);
        List<IframeResponse> charts  = grafanaService.getFilterCharts(dashboardUid, filterMap);
        return ResponseEntity.ok(charts);
    }
}
