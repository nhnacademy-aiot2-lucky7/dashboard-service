package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.dto.GrafanaDashboardInfo;
import com.nhnacademy.dashboard.dto.GrafanaDashboardResponse;
import com.nhnacademy.dashboard.dto.GrafanaFolder;
import com.nhnacademy.dashboard.dto.GrafanaFolderResponse;
import com.nhnacademy.dashboard.exception.NotFoundException;
import com.nhnacademy.dashboard.service.impl.GrafanaServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GrafanaController {

    private final GrafanaServiceImpl grafanaService;

    @PostMapping("/folders/{folderTitle}/add/{dashboardTitle}")
    @Operation(summary = "새로운 대시보드 추가")
    public ResponseEntity<Void> createDashboard(
            @PathVariable String folderTitle,
            @PathVariable String dashboardTitle) {

        grafanaService.createDashboard(folderTitle, dashboardTitle);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @GetMapping("/folders")
    @Operation(summary = "모든 폴더 조회")
    public List<GrafanaFolder> getFolders(){
        List<GrafanaFolder> response = grafanaService.getAllFolders();
        if(response.isEmpty()){
            throw new NotFoundException("getFolders is not Found");
        }

        return response;
    }

    @GetMapping("/folders/name/{folderTitle}")
    @Operation(summary ="폴더명으로 대시보드 이름 조회")
    public List<String> getDashboardName(@PathVariable String folderTitle) {
        List<GrafanaDashboardInfo> dashboards = grafanaService.getDashboardByTitle(folderTitle);

        return dashboards.stream()
                .map(GrafanaDashboardInfo::getTitle)
                .toList();
    }

    @GetMapping(value = "/folders/{folderTitle}")
    @Operation(summary ="폴더명으로 모든 대시보드 조회")
    public List<GrafanaFolderResponse> getIframeUrlsToFolder(@PathVariable String folderTitle) {

        List<GrafanaDashboardInfo> dashboards = grafanaService.getDashboardByTitle(folderTitle);

        log.info("getIframeUrlsToFolder -> dashboards: {}", dashboards);
        return dashboards.stream()
                .map(d -> GrafanaFolderResponse.ofGrafanaResponse(d.getTitle(), d.getUid()))
                .toList();
    }


    // 차트 조회
    @GetMapping(value = "/folders/{folderTitle}/dashboards/{dashboardName}/chart")
    @Operation(summary = "차트 조회")
    public ResponseEntity<List<GrafanaDashboardResponse>> getChartByName(
            @PathVariable String folderTitle,
            @PathVariable String dashboardName) {

        return grafanaService.getChart(folderTitle, dashboardName);
    }


    // 차트 이름 조회
    @GetMapping(value = "/folders/{folderTitle}/dashboards/{dashboardTitle}/chart/name")
    @Operation(summary ="차트 이름 조회")
    public List<String> getChartNameByName(
            @PathVariable String folderTitle,
            @PathVariable String dashboardTitle) {

        ResponseEntity<List<GrafanaDashboardResponse>> responses = grafanaService.getChart(folderTitle, dashboardTitle);

        List<GrafanaDashboardResponse> body = responses.getBody();
        if (body == null || body.isEmpty()) {
            throw new NotFoundException("getChartNameByName -> responses is null or empty");
        }

        return body.stream()
                .map(GrafanaDashboardResponse::getTitle)
                .toList();
    }

    // 🌟 대시보드 on/off 필터링
    @GetMapping("/folders/{folderTitle}/dashboards/{dashboardTitle}/filtered-chart")
    @Operation(summary ="메인페이지 on/off 필터 조회")
    public ResponseEntity<List<GrafanaFolderResponse>> getDashboardCharts(
            @PathVariable String folderTitle,
            @PathVariable String dashboardTitle,
            @RequestParam(name = "filter", required = false) String filter
    ) {
        Map<String, String> filterMap = grafanaService.parseFilter(filter);
        List<GrafanaFolderResponse> charts  = grafanaService.getFilterCharts(folderTitle, dashboardTitle, filterMap);
        return ResponseEntity.ok(charts);
    }

}
