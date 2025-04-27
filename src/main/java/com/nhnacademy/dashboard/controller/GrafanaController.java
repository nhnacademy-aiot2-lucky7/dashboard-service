package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.dto.GrafanaDashboardInfo;
import com.nhnacademy.dashboard.dto.response.GrafanaChartResponse;
import com.nhnacademy.dashboard.dto.response.GrafanaDashboardResponse;
import com.nhnacademy.dashboard.dto.GrafanaFolder;
import com.nhnacademy.dashboard.dto.response.GrafanaFolderResponse;
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

    @PostMapping("/f/{folderTitle}/d/add/{dashboardTitle}")
    @Operation(summary = "새로운 대시보드 추가")
    public ResponseEntity<Void> createDashboard(
            @PathVariable String folderTitle,
            @PathVariable String dashboardTitle) {

        grafanaService.createDashboard(folderTitle, dashboardTitle);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    // POST http://localhost:10243/api/folders/{folderTitle}/dashboards/{air}/chart?title=chart1&sensor=co&&aggregation=mean&time=2d
    @PostMapping("/f/{folderTitle}/d/{dashboardTitle}/c/add")
    @Operation(summary = "새로운 차트 추가")
    public ResponseEntity<GrafanaChartResponse> createChart(
            @PathVariable String folderTitle,
            @PathVariable String dashboardTitle,
            @RequestParam String title,
            @RequestParam String sensor,
            @RequestParam String aggregation,
            @RequestParam String time
            ){

        GrafanaChartResponse response =grafanaService.createChart(folderTitle, dashboardTitle, title, sensor, aggregation, time);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
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

    @GetMapping("/f/name/{folderTitle}")
    @Operation(summary ="폴더명으로 대시보드 이름 조회")
    public List<String> getDashboardName(@PathVariable String folderTitle) {
        List<GrafanaDashboardInfo> dashboards = grafanaService.getDashboardByTitle(folderTitle);

        return dashboards.stream()
                .map(GrafanaDashboardInfo::getTitle)
                .toList();
    }

    @GetMapping(value = "/f/{folderTitle}")
    @Operation(summary ="폴더명으로 모든 대시보드 조회")
    public List<GrafanaFolderResponse> getIframeUrlsToFolder(@PathVariable String folderTitle) {

        List<GrafanaDashboardInfo> dashboards = grafanaService.getDashboardByTitle(folderTitle);

        log.info("getIframeUrlsToFolder -> dashboards: {}", dashboards);
        return dashboards.stream()
                .map(d -> GrafanaFolderResponse.ofGrafanaResponse(d.getTitle(), d.getUid()))
                .toList();
    }


    // 차트 조회
    @GetMapping(value = "/f/{folderTitle}/d/{dashboardName}/c")
    @Operation(summary = "차트 조회")
    public ResponseEntity<List<GrafanaDashboardResponse>> getChartByName(
            @PathVariable String folderTitle,
            @PathVariable String dashboardName) {

        return grafanaService.getChart(folderTitle, dashboardName);
    }


    // 차트 이름 조회
    @GetMapping(value = "/f/{folderTitle}/d/{dashboardTitle}/c/name")
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

    // 대시보드 on/off 필터링
    @GetMapping("/f/{folderTitle}/d/{dashboardTitle}/filtered-chart")
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

    // 🌟차트 수정하기
    // POST http://localhost:10243/api/f/sample/d/sampleG/update/c/aGRAPH?title=a_update
    @PostMapping("/f/{folderTitle}/d/{dashboardTitle}/update/c/{chartTitle}")
    @Operation(summary = "차트 이름 수정하기")
    public ResponseEntity<GrafanaChartResponse> updateChart(
            @PathVariable String folderTitle,
            @PathVariable String dashboardTitle,
            @PathVariable String chartTitle,
            @RequestParam String title
    ){
        GrafanaChartResponse response = grafanaService.updateChartName(folderTitle, dashboardTitle, chartTitle, title);

        return ResponseEntity
                .ok(response);
    }

}
