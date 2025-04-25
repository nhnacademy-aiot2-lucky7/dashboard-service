package com.nhnacademy.dashboard.service.impl;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.dto.*;
import com.nhnacademy.dashboard.dto.response.GrafanaChartResponse;
import com.nhnacademy.dashboard.exception.NotFoundException;
import com.nhnacademy.dashboard.dto.request.GrafanaCreateDashboardRequest;
import com.nhnacademy.dashboard.dto.response.GrafanaDashboardResponse;
import com.nhnacademy.dashboard.dto.response.GrafanaFolderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrafanaServiceImpl {

    private final GrafanaApi grafanaApi;
    public static final String TYPE = "dash-db";

    // 대시보드 생성
    public void createDashboard(String folderTitle, String title) {

        GrafanaCreateDashboardRequest.Dashboard dashboard = new GrafanaCreateDashboardRequest.Dashboard(title);
        int folderId = getFolderIdByTitle(folderTitle);
        GrafanaCreateDashboardRequest request = new GrafanaCreateDashboardRequest(dashboard, folderId);
        grafanaApi.createDashboard(request);
    }


    // 모든 폴더 조회
    public List<GrafanaFolder> getAllFolders() {
        List<GrafanaFolder> folders = grafanaApi.getAllFolders();

        List<GrafanaFolder> filtered = folders.stream()
                .filter(folder -> folder.getId() >= 0).toList();

        log.info("필터링된 response: {}", filtered);
        return filtered;
    }

    // 폴더명으로 folderIds조회
    public int getFolderIdByTitle(String folderTitle) {
        List<GrafanaFolder> folders = grafanaApi.getAllFolders();

        if (folders.isEmpty()) {
            throw new NotFoundException("folderTitle is NotFound : " + folderTitle);
        }

        return folders.stream()
                .filter(f -> folderTitle.equals(f.getTitle()))
                .findFirst()
                .map(GrafanaFolder::getId)
                .orElse(0);
    }

    // 폴더명으로 대시보드 검색
    public List<GrafanaDashboardInfo> getDashboardByTitle(String folderTitle) {
        int folderId = getFolderIdByTitle(folderTitle);

        return grafanaApi.searchDashboards(folderId, TYPE);
    }

    // 폴더명으로 UID 찾기
    public String getFolderUidByTitle(String folderTitle) {
        List<GrafanaFolder> folders = grafanaApi.getAllFolders();

        if (folders.isEmpty()) {
            throw new NotFoundException("folderTitle is NotFound : " + folderTitle);
        }
        return folders.stream()
                .filter(f -> folderTitle.equals(f.getTitle()))
                .findFirst()
                .map(GrafanaFolder::getUid)
                .orElse(null);
    }

    // 차트 조회
    public ResponseEntity<List<GrafanaDashboardResponse>> getChart(String folderTitle, String dashboardTitle) {
        List<GrafanaDashboardInfo> dashboardInfos = getDashboardByTitle(folderTitle);

        GrafanaDashboardInfo targetDashboard = dashboardInfos.stream()
                .filter(d -> d.getTitle().equals(dashboardTitle))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Dashboard not found: " + dashboardTitle));

        String uid = targetDashboard.getUid();

        ResponseEntity<GrafanaDashboardPanel> panelResponseEntity = grafanaApi.getChart(uid);
        GrafanaDashboardPanel panel = panelResponseEntity.getBody();

        assert panel != null;
        List<GrafanaDashboardResponse> responseList = panel.getDashboard().getPanels().stream()
                .map(GrafanaDashboardResponse::from)
                .toList();

        return ResponseEntity.ok(responseList);
    }

    // panel : off -> map형태에 넣어주기
    public Map<String, String> parseFilter(String filter) {
        Map<String, String> result = new HashMap<>();

        for (String entry : filter.split(",")) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                result.put(parts[0].trim(), parts[1].trim());
            }
        }

        return result;
    }

    // filter된 차트 조회
    public List<GrafanaFolderResponse> getFilterCharts(
            String folderTitle,
            String dashboardTitle,
            Map<String, String> filterMap) {

        List<GrafanaDashboardInfo> dashboards = getDashboardByTitle(folderTitle);
        if (dashboards.isEmpty()) {
            throw new NotFoundException("Dashboard with title " + dashboardTitle + " not found");
        }

        GrafanaDashboardInfo dashboardInfo = dashboards.getFirst();
        String uid = dashboardInfo.getUid();

        log.info("getFilterCharts -> uid: {}", uid);
        GrafanaDashboardPanel detail = grafanaApi.getDashboardDetail(uid);

        if (detail == null || detail.getDashboard() == null) {
            throw new NotFoundException("Dashboard details not found for uid: " + uid);
        }

        log.info("getFilterCharts -> detail: {}", detail);
        List<GrafanaFolderResponse> result = new ArrayList<>();

        for (GrafanaPanel panel : detail.getDashboard().getPanels()) {
            String panelTitle = panel.getTitle();

            if ("off".equalsIgnoreCase(filterMap.get(panelTitle))) {
                continue;
            }

            result.add(GrafanaFolderResponse.ofGrafanaResponse(panelTitle, uid));
        }

        log.info("result:{}", result);
        return result;
    }


    // 차트 추가생성하기
    public GrafanaChartResponse createChart(String folderTitle, String dashboardTitle,
                                            String panelTitle, String sensor, String aggregation, String time) {
        // Flux 쿼리 생성
        String fluxQuery = String.format("""
        from(bucket: "test")
          |> range(start: -%s)
          |> filter(fn: (r) => r["_measurement"] == "airSensors")
          |> filter(fn: (r) => r["_field"] == "%s")
          |> aggregateWindow(every: 1m, fn: %s, createEmpty: true)
          |> yield(name: "%s")
        """, time, sensor, aggregation, aggregation);

        log.info("Create CHART query: {}", fluxQuery);
        Map<String, Object> dashboard = getStringObjectMap(dashboardTitle, panelTitle, fluxQuery);

        String folderUid = getFolderUidByTitle(folderTitle);
        Map<String, Object> request = Map.of(
                "dashboard", dashboard,
                "folderUid", folderUid,
                "overwrite", true
        );

        log.info("Create CHART -> request:{}", request);

        return grafanaApi.createChart(request).getBody();
    }

    private static Map<String, Object> getStringObjectMap(String dashboardTitle, String panelTitle, String fluxQuery) {
        Map<String, Object> panel = new HashMap<>();
        panel.put("id", null);
        panel.put("type", "timeseries");
        panel.put("title", panelTitle);
        panel.put("gridPos", Map.of("x", 0, "y", 0, "w", 12, "h", 8));
        panel.put("targets", List.of(Map.of(
                "refId", "A",
                "datasource", Map.of("type", "influxdb", "uid", "o4aKnEJNk"),
                "query", fluxQuery,
                "queryType", "flux",
                "format", "time_series"
        )));
        panel.put("datasource", Map.of("type", "influxdb", "uid", "o4aKnEJNk"));

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