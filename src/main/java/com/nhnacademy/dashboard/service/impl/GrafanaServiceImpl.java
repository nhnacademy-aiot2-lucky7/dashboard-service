package com.nhnacademy.dashboard.service.impl;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.dto.*;
import com.nhnacademy.dashboard.dto.response.GrafanaDashboardResponse;
import com.nhnacademy.dashboard.exception.NotFoundException;
import com.nhnacademy.dashboard.dto.request.GrafanaCreateDashboardRequest;
import com.nhnacademy.dashboard.dto.response.GrafanaSimpleDashboardResponse;
import com.nhnacademy.dashboard.dto.response.GrafanaFolderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrafanaServiceImpl {

    private final GrafanaApi grafanaApi;
    public static final String TYPE = "dash-db";
    private static final String INFLUXDB_UID = "o4aKnEJNk";
    private static final int GRID_WIDTH = 12;
    private static final int GRID_HEIGHT = 8;

    /**
     * 새로운 대시보드를 생성합니다.
     *
     * @param folderTitle 폴더 이름
     * @param title       생성할 대시보드 제목
     */
    public void createDashboard(String folderTitle, String title) {
        int folderId = getFolderIdByTitle(folderTitle);
        grafanaApi.createDashboard(new GrafanaCreateDashboardRequest(new GrafanaCreateDashboardRequest.Dashboard(title), folderId));
    }


    /**
     * 모든 폴더 목록을 조회합니다.
     *
     * @return 폴더 리스트
     */
    public List<GrafanaFolder> getAllFolders() {
        List<GrafanaFolder> folders = grafanaApi.getAllFolders();

        log.info("전체 폴더: {}", folders.toString());
        return folders;
    }

    /**
     * 폴더 이름을 통해 대시보드 리스트를 조회합니다.
     *
     * @param folderTitle 폴더 이름
     * @return 폴더에 포함된 대시보드 리스트
     */
    public List<GrafanaDashboardInfo> getDashboardByTitle(String folderTitle) {
        List<GrafanaDashboardInfo> dashboards = grafanaApi.searchDashboards(getFolderIdByTitle(folderTitle), TYPE);
        log.info("Retrieved dashboards: {}", dashboards);
        return dashboards;
    }

    /**
     * 특정 폴더와 대시보드 이름으로 차트 목록을 조회합니다.
     *
     * @param folderTitle    폴더 이름
     * @param dashboardTitle 대시보드 이름
     * @return 차트 리스트
     */
    public ResponseEntity<List<GrafanaSimpleDashboardResponse>> getChart(String folderTitle, String dashboardTitle) {

        String uid = findDashboardUid(folderTitle, dashboardTitle);
        GrafanaDashboardPanel panel = grafanaApi.getChart(uid).getBody();

        if (panel == null || panel.getDashboard() == null) {
            throw new NotFoundException("Dashboard panel not found for UID: " + uid);
        }

        List<GrafanaSimpleDashboardResponse> responseList = panel.getDashboard().getPanels().stream()
                .map(GrafanaSimpleDashboardResponse::from)
                .toList();

        return ResponseEntity.ok(responseList);
    }


    /**
     * 필터 문자열을 파싱하여 Map 형태로 변환합니다.
     *
     * @param filter 필터 문자열 (ex. "chart1:on, chart2:off")
     * @return 필터 Map
     */
    public Map<String, String> parseFilter(String filter) {
        return Arrays.stream(filter.split(","))
                .map(String::trim)
                .map(entry -> entry.split(":"))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0].trim(), parts -> parts[1].trim()));
    }

    /**
     * 필터링 조건에 맞는 차트를 조회합니다.
     *
     * @param folderTitle    폴더 이름
     * @param dashboardTitle 대시보드 이름
     * @param filterMap      필터링 조건
     * @return 필터링된 차트 리스트
     */
    public List<GrafanaFolderResponse> getFilterCharts(
            String folderTitle,
            String dashboardTitle,
            Map<String, String> filterMap) {

        String uid = findDashboardUid(folderTitle, dashboardTitle);
        GrafanaDashboardPanel detail = grafanaApi.getDashboardDetail(uid);

        if (detail == null || detail.getDashboard() == null) {
            throw new NotFoundException("Dashboard details not found for uid: " + uid);
        }

        return detail.getDashboard().getPanels().stream()
                .filter(panel -> !"off".equalsIgnoreCase(filterMap.get(panel.getTitle())))
                .map(panel -> GrafanaFolderResponse.ofGrafanaResponse(panel.getTitle(), uid))
                .toList();
    }


    /**
     * 새로운 차트를 생성합니다.
     *
     * @param folderTitle    폴더 이름
     * @param dashboardTitle 대시보드 이름
     * @param panelTitle     생성할 패널 이름
     * @param sensor         센서 이름
     * @param aggregation    집계 함수
     * @param time           조회 시간 범위
     * @return 생성된 대시보드 응답
     */
    public GrafanaDashboardResponse createChart(String folderTitle, String dashboardTitle,
                                                String panelTitle, String measurement, String sensor, String type, String aggregation, String time) {
        String fluxQuery = generateFluxQuery(measurement, sensor, aggregation, time);
        Map<String, Object> dashboard = buildDashboardRequest(type, dashboardTitle, panelTitle, fluxQuery);
        Map<String, Object> request = Map.of(
                "dashboard", dashboard,
                "folderUid", getFolderUidByTitle(folderTitle),
                "overwrite", false
        );

        log.info("Create CHART -> request: {}", request);
        return grafanaApi.createChart(request).getBody();
    }

    /**
     * 차트 제목을 수정합니다.
     *
     * @param folderTitle   폴더 이름
     * @param dashboardTitle 대시보드 이름
     * @param chartTitle    수정할 차트 제목
     * @param updateTitle   수정된 차트 제목
     * @return 수정된 대시보드 응답
     */
    public GrafanaDashboardResponse updateChartName(String folderTitle, String dashboardTitle,
                                                    String chartTitle, String updateTitle){

        GrafanaDashboard dashboard = getDashboardInfo(folderTitle, dashboardTitle);
        log.info("updateChartName -> 대시보드 title, uid:{},{}", dashboard.getDashboard().getTitle(), dashboard.getDashboard().getUid());

        dashboard.getDashboard().getPanels().stream()
                .filter(panel -> panel.getTitle().equals(chartTitle))
                .forEach(panel -> panel.setTitle(updateTitle));

        log.info("UPDATE CHART -> dashboard: {}", dashboard);
        return grafanaApi.update(dashboard);
    }

    // 🌟차트 쿼리 수정🌟 <- 패널이 여러개일 경우
    public GrafanaDashboardResponse updateChart(String folderTitle, String dashboardTitle,
                                                String title, String measurement, String field, String type, String aggregation, String time){
        String fluxQuery = generateFluxQuery(measurement, field, aggregation, time);
        Map<String, Object> dashboard = buildDashboardRequest(type, dashboardTitle, title, fluxQuery);
        Map<String, Object> request = Map.of(
                "dashboard", dashboard,
                "folderUid", getFolderUidByTitle(folderTitle),
                "overwrite", true
        );

        log.info("UPDATE CHART -> request: {}", request);
        return grafanaApi.createChart(request).getBody();
    }

    /**
     * 대시보드 제목을 수정합니다.
     *
     * @param folderTitle    폴더 이름
     * @param dashboardTitle 수정할 대시보드 제목
     * @param updateTitle    수정된 대시보드 제목
     * @return 수정된 대시보드 응답
     */
    public GrafanaDashboardResponse updateDashboardName(String folderTitle, String dashboardTitle, String updateTitle){
        GrafanaDashboard dashboard = getDashboardInfo(folderTitle, dashboardTitle);
        log.info("updateDashboard -> 대시보드 title, uid:{},{}", dashboard.getDashboard().getTitle(), dashboard.getDashboard().getUid());

        if (dashboard.getDashboard().getTitle().equals(dashboardTitle)) {
            dashboard.getDashboard().setTitle(updateTitle);
        }

        return grafanaApi.update(dashboard);
    }

    /**
     * 주어진 폴더와 대시보드 이름에 해당하는 대시보드 정보를 반환합니다.
     *
     * @param folderTitle    폴더 이름
     * @param dashboardTitle 대시보드 이름
     * @return 해당 대시보드 정보
     */
    private GrafanaDashboard getDashboardInfo(String folderTitle, String dashboardTitle) {
        String uid = findDashboardUid(folderTitle, dashboardTitle);
        GrafanaDashboard dashboard = grafanaApi.getDashboardInfo(uid);
        if (dashboard == null || dashboard.getDashboard() == null) {
            throw new NotFoundException("Dashboard not found for UID: " + uid);
        }
        return dashboard;
    }

    /**
     * 주어진 폴더와 대시보드 이름에 해당하는 대시보드 UID를 찾습니다.
     *
     * @param folderTitle    폴더 이름
     * @param dashboardTitle 대시보드 이름
     * @return 대시보드 UID
     */
    private String findDashboardUid(String folderTitle, String dashboardTitle) {
        return getDashboardByTitle(folderTitle).stream()
                .filter(d -> dashboardTitle.equals(d.getTitle()))
                .findFirst()
                .map(GrafanaDashboardInfo::getUid)
                .orElseThrow(() -> new NotFoundException("Dashboard not found: " + dashboardTitle));
    }

    /**
     * 주어진 폴더 제목에 해당하는 폴더를 반환합니다.
     *
     * @param folderTitle 폴더 제목
     * @return 폴더 객체
     */
    private GrafanaFolder getFolderByTitle(String folderTitle) {
        return grafanaApi.getAllFolders().stream()
                .filter(folder -> folderTitle.equals(folder.getTitle()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Folder not found: " + folderTitle));
    }

    /**
     * 폴더 제목에 해당하는 폴더의 ID를 반환합니다.
     *
     * @param folderTitle 폴더 제목
     * @return 폴더 ID
     */
    private int getFolderIdByTitle(String folderTitle) {
        return getFolderByTitle(folderTitle).getId();
    }

    /**
     * 폴더 제목에 해당하는 폴더의 UID를 반환합니다.
     *
     * @param folderTitle 폴더 제목
     * @return 폴더 UID
     */
    private String getFolderUidByTitle(String folderTitle) {
        return getFolderByTitle(folderTitle).getUid();
    }

    /**
     * Flux 쿼리를 생성합니다.
     *
     * @param measurement 측정 항목
     * @param sensor     센서 이름
     * @param aggregation 집계 함수
     * @param time       시간 범위
     * @return 생성된 Flux 쿼리
     */
    private String generateFluxQuery(String measurement, String sensor, String aggregation, String time) {
        return String.format("""
            from(bucket: "test")
              |> range(start: -%s)
              |> filter(fn: (r) => r["_measurement"] == "%s")
              |> filter(fn: (r) => r["_field"] == "%s")
              |> aggregateWindow(every: 1m, fn: %s, createEmpty: true)
              |> yield(name: "%s")
            """, time, measurement, sensor, aggregation, aggregation);
    }

    /**
     * 대시보드 요청을 위한 기본 구조를 만듭니다.
     *
     * @param dashboardTitle 대시보드 제목
     * @param panelTitle     패널 제목
     * @param fluxQuery      Flux 쿼리
     * @return 대시보드 요청 정보
     */
    private Map<String, Object> buildDashboardRequest(String type, String dashboardTitle, String panelTitle, String fluxQuery) {
        Map<String, Object> panel = new HashMap<>();
        panel.put("id", null);
        panel.put("type", type);
        panel.put("title", panelTitle);
               panel.put( "gridPos", Map.of("x", 0, "y", 0, "w", GRID_WIDTH, "h", GRID_HEIGHT));
                panel.put("targets", List.of(Map.of(
                        "refId", "A",
                        "datasource", Map.of("type", "influxdb", "uid", INFLUXDB_UID),
                        "query", fluxQuery,
                        "queryType", "flux",
                        "format", "time_series"
                )));
                panel.put("datasource", Map.of("type", "influxdb", "uid", INFLUXDB_UID));

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("id", null);
        dashboard.put("uid", null);
        dashboard.put("title", dashboardTitle);
        dashboard.put("panels", List.of(panel));
        dashboard.put("schemaVersion", 41);
        dashboard.put("version", 0);

        return dashboard;
    }
}