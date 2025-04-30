package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.dto.GrafanaDashboardInfo;
import com.nhnacademy.dashboard.dto.request.ChartCreateRequest;
import com.nhnacademy.dashboard.dto.request.ChartUpdateRequest;
import com.nhnacademy.dashboard.dto.response.GrafanaDashboardResponse;
import com.nhnacademy.dashboard.dto.response.GrafanaSimpleDashboardResponse;
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

    /**
     * 새로운 대시보드를 추가하는 API.
     *
     * @param folderTitle 대시보드를 추가할 폴더의 제목
     * @param dashboardTitle 추가할 대시보드의 제목
     * @return 생성된 대시보드에 대한 HTTP 응답 (201 Created)
     */
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

    /**
     * 새로운 차트를 추가하는 API.
     *
     * @param folderTitle 대시보드가 속한 폴더의 제목
     * @param dashboardTitle 차트를 추가할 대시보드의 제목
     * @param title 차트의 제목
     * @param measurement 측정할 데이터 항목
     * @param field 측정할 센서
     * @param aggregation 차트의 집계 방식
     * @param time 차트의 시간 범위
     * @return 생성된 차트에 대한 응답 (201 Created)
     */
    @PostMapping("/f/{folderTitle}/d/{dashboardTitle}/c/add")
    @Operation(summary = "새로운 차트 추가")
    public ResponseEntity<GrafanaDashboardResponse> createChart(
            @PathVariable String folderTitle,
            @PathVariable String dashboardTitle,
            @RequestParam String title,
            @RequestParam String measurement,
            @RequestParam List<String> field,
            @RequestParam(defaultValue = "timeseries") String type,
            @RequestParam String aggregation,
            @RequestParam String time
            ) {

        ChartCreateRequest request = new ChartCreateRequest(folderTitle, dashboardTitle, measurement, field, type, aggregation, time);
        GrafanaDashboardResponse response =grafanaService.createChart(title, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * 모든 폴더를 조회하는 API.
     *
     * @return 모든 폴더 목록
     * @throws NotFoundException 폴더가 없을 경우 예외 발생
     */
    @GetMapping("/folders")
    @Operation(summary = "모든 폴더 조회")
    public List<GrafanaFolder> getFolders(){
        return grafanaService.getAllFolders();
    }

    /**
     * 폴더명으로 대시보드 제목을 조회하는 API.
     *
     * @param folderTitle 대시보드가 속한 폴더의 제목
     * @return 해당 폴더에 있는 대시보드 제목 목록
     */
    @GetMapping("/f/name/{folderTitle}")
    @Operation(summary ="폴더명으로 대시보드 이름 조회")
    public List<String> getDashboardName(@PathVariable String folderTitle) {
        List<GrafanaDashboardInfo> dashboards = grafanaService.getDashboardByTitle(folderTitle);

        return dashboards.stream()
                .map(GrafanaDashboardInfo::getTitle)
                .toList();
    }

    /**
     * 폴더명으로 해당 폴더의 모든 대시보드를 조회하는 API.
     *
     * @param folderTitle 대시보드가 속한 폴더의 제목
     * @return 폴더에 포함된 모든 대시보드에 대한 응답
     */
    @GetMapping(value = "/f/{folderTitle}")
    @Operation(summary ="폴더명으로 모든 대시보드 조회")
    public List<GrafanaFolderResponse> getIframeUrlsToFolder(@PathVariable String folderTitle) {

        List<GrafanaDashboardInfo> dashboards = grafanaService.getDashboardByTitle(folderTitle);

        log.info("getIframeUrlsToFolder -> dashboards: {}", dashboards);
        return dashboards.stream()
                .map(d -> GrafanaFolderResponse.ofGrafanaResponse(d.getTitle(), d.getUid()))
                .toList();
    }


    /**
     * 차트를 조회하는 API.
     *
     * @param folderTitle 대시보드가 속한 폴더의 제목
     * @param dashboardName 조회할 대시보드의 제목
     * @return 해당 대시보드의 차트 목록
     */
    @GetMapping(value = "/f/{folderTitle}/d/{dashboardName}/c")
    @Operation(summary = "차트 조회")
    public ResponseEntity<List<GrafanaSimpleDashboardResponse>> getChartByName(
            @PathVariable String folderTitle,
            @PathVariable String dashboardName) {

        return grafanaService.getChart(folderTitle, dashboardName);
    }


    /**
     * 차트 이름을 조회하는 API.
     *
     * @param folderTitle 대시보드가 속한 폴더의 제목
     * @param dashboardTitle 조회할 대시보드의 제목
     * @return 해당 대시보드에 포함된 차트 이름 목록
     * @throws NotFoundException 차트가 없을 경우 예외 발생
     */
    @GetMapping(value = "/f/{folderTitle}/d/{dashboardTitle}/c/name")
    @Operation(summary ="차트 이름 조회")
    public List<String> getChartNameByName(
            @PathVariable String folderTitle,
            @PathVariable String dashboardTitle) {

        ResponseEntity<List<GrafanaSimpleDashboardResponse>> responses = grafanaService.getChart(folderTitle, dashboardTitle);

        List<GrafanaSimpleDashboardResponse> body = responses.getBody();
        if (body == null || body.isEmpty()) {
            throw new NotFoundException("getChartNameByName -> responses is null or empty");
        }

        return body.stream()
                .map(GrafanaSimpleDashboardResponse::getTitle)
                .toList();
    }

    /**
     * 대시보드 차트의 필터링된 목록을 조회하는 API.
     *
     * @param folderTitle 대시보드가 속한 폴더의 제목
     * @param dashboardTitle 필터링할 대시보드의 제목
     * @param filter 차트 필터
     * @return 필터링된 차트 목록
     */
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

    /**
     * 대시보드의 이름을 수정하는 API.
     *
     * @param folderTitle 대시보드가 속한 폴더의 제목
     * @param dashboardTitle 수정할 대시보드의 제목
     * @param title 수정할 새로운 대시보드 제목
     * @return 수정된 대시보드에 대한 응답
     */
    @PostMapping("/f/{folderTitle}/d/update/{dashboardTitle}")
    @Operation(summary = "대시보드 이름 수정")
    public ResponseEntity<GrafanaDashboardResponse> updateDashboard(
            @PathVariable String folderTitle,
            @PathVariable String dashboardTitle,
            @RequestParam String title
    ){
        GrafanaDashboardResponse response = grafanaService.updateDashboardName(folderTitle, dashboardTitle, title);
        return ResponseEntity
                .ok(response);
    }

    /**
     * 차트 쿼리를 수정하는 API입니다.
     * <p>
     * 주어진 폴더 제목, 대시보드 제목, 차트 제목에 대한 차트 쿼리를 수정합니다. 수정할 값들은 요청 파라미터로 전달됩니다.
     * </p>
     *
     * @param folderTitle 수정할 차트가 위치한 폴더의 제목
     * @param dashboardTitle 수정할 차트가 위치한 대시보드의 제목
     * @param chartTitle 수정할 차트의 제목
     * @param title 차트의 새로운 제목
     * @param measurement 차트의 새로운 측정 항목
     * @param field 차트의 새로운 필드 목록
     * @param style 차트의 스타일
     * @param aggregation 차트의 집계 방식
     * @param time 차트의 시간 범위
     * @return 수정된 차트의 응답 정보
     */
    @PostMapping("/f/{folderTitle}/d/{dashboardTitle}/edit/c/{chartTitle}")
    @Operation(summary = "차트 쿼리 수정")
    public ResponseEntity<GrafanaDashboardResponse> updateChart(
            @PathVariable String folderTitle,
            @PathVariable String dashboardTitle,
            @PathVariable String chartTitle,
            @RequestParam String title,
            @RequestParam String measurement,
            @RequestParam List<String> field,
            @RequestParam String style,
            @RequestParam String aggregation,
            @RequestParam String time
    ){
        GrafanaDashboardResponse response = grafanaService.updateChart(
                new ChartUpdateRequest(folderTitle, dashboardTitle, chartTitle, title, measurement, field, style, aggregation, time));

        return ResponseEntity
                .ok(response);
    }

    /**
     * 폴더를 삭제하는 API입니다.
     * <p>
     * 주어진 폴더 제목에 해당하는 폴더를 삭제합니다.
     * </p>
     *
     * @param folderTitle 삭제할 폴더의 제목
     * @return HTTP 상태 코드 204(No Content)와 함께 응답
     */
    @DeleteMapping("/remove/{folderTitle}")
    @Operation(summary = "폴더 삭제")
    public ResponseEntity<Void> deleteFolder(@PathVariable String folderTitle){
        grafanaService.removeFolder(folderTitle);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    /**
     * 대시보드를 삭제하는 API입니다.
     * <p>
     * 주어진 폴더 제목과 대시보드 제목에 해당하는 대시보드를 삭제합니다.
     * </p>
     *
     * @param folderTitle 삭제할 대시보드가 위치한 폴더의 제목
     * @param dashboardTitle 삭제할 대시보드의 제목
     * @return HTTP 상태 코드 204(No Content)와 함께 응답
     */
    @DeleteMapping("/remove/{folderTitle}/d/{dashboardTitle}")
    @Operation(summary = "대시보드 삭제")
    public ResponseEntity<Void> deleteDashboard(@PathVariable String folderTitle, @PathVariable String dashboardTitle){
        grafanaService.removeDashboard(folderTitle, dashboardTitle);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 차트를 삭제하는 API입니다.
     * <p>
     * 주어진 폴더 제목, 대시보드 제목, 차트 제목에 해당하는 차트를 삭제합니다.
     * </p>
     *
     * @param folderTitle 삭제할 차트가 위치한 폴더의 제목
     * @param dashboardTitle 삭제할 차트가 위치한 대시보드의 제목
     * @param chartTitle 삭제할 차트의 제목
     * @return 삭제된 차트에 대한 응답 정보
     */
    @DeleteMapping("/remove/{folderTitle}/d/{dashboardTitle}/c/{chartTitle}")
    @Operation(summary = "차트 삭제")
    public ResponseEntity<GrafanaDashboardResponse> deleteChart(@PathVariable String folderTitle, @PathVariable String dashboardTitle, @PathVariable String chartTitle){
        GrafanaDashboardResponse response = grafanaService.removeChart(folderTitle, dashboardTitle, chartTitle);

        return ResponseEntity.ok(response);
    }
}
