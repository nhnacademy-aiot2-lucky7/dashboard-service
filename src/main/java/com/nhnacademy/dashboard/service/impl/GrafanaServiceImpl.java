package com.nhnacademy.dashboard.service.impl;

import com.nhnacademy.dashboard.adapter.GrafanaAdapter;
import com.nhnacademy.dashboard.dto.*;
import com.nhnacademy.dashboard.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrafanaServiceImpl {

    private static final String AUTHORIZATION = "Bearer ";
    private final GrafanaAdapter grafanaAdapter;
    @Value("${grafana.api-key}")
    private String apiKey;

    // 모든 폴더 조회
    public List<GrafanaFolder> getAllFolders() {
        List<GrafanaFolder> folders = grafanaAdapter.getAllFolders(AUTHORIZATION + apiKey);

        List<GrafanaFolder> filtered = folders.stream()
                .filter(folder -> folder.getId() >= 0).toList();

        log.info("필터링된 response: {}", filtered);
        return filtered;
    }

    // 폴더명으로 UID 찾기
    public String getFolderUidByTitle(String folderTitle) {
        List<GrafanaFolder> folders = grafanaAdapter.getAllFolders(AUTHORIZATION + apiKey);

        if (folders.isEmpty()) {
            throw new NotFoundException("folderTitle is NotFound : " + folderTitle);
        }
        return folders.stream()
                .filter(f -> folderTitle.equals(f.getTitle()))
                .findFirst()
                .map(GrafanaFolder::getUid)
                .orElse(null);
    }

    // 대시보드명으로 UID 찾기
    public String getDashboardNameUidByTitle(String dashboardTitle) {

        List<GrafanaDashboardInfo> dashboards = getDashboard(dashboardTitle);

        if (dashboards.isEmpty()) {
            throw new NotFoundException("dashboardsTitle is NotFound : " + dashboardTitle);
        }

        return dashboards.stream()
                .filter(d -> dashboardTitle.equals(d.getTitle()))
                .map(GrafanaDashboardInfo::getUid)
                .findFirst()
                .orElse(null);
    }

    // 폴더UID로 대시보드 목록 조회
    public List<GrafanaDashboardInfo> getDashboardsInFolder(String folderUid) {
        if (folderUid == null || folderUid.trim().isEmpty()) {
            throw new NotFoundException("folderUid is NotFound : " + folderUid);
        }

        List<GrafanaDashboardInfo> dashboards = getDashboard(folderUid);

        if (dashboards == null || dashboards.isEmpty()) {
            throw new NotFoundException("No dashboards found for folderUid : " + folderUid);
        }

        return dashboards;
    }

    // panel : off -> map형태에 넣어주기
    public Map<String, String> parseFilter(String filter) {
        Map<String, String> result = new HashMap<>();

        if (filter == null || filter.isBlank()) return result;

        for (String entry : filter.split(",")) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                result.put(parts[0].trim(), parts[1].trim());
            }
        }

        return result;
    }

    // filter된 차트 조회
    public List<GrafanaResponse> getDashboardCharts(String dashboardTitle, Map<String, String> filterMap) {
        List<GrafanaDashboardInfo> dashboardUid = getDashboardsInFolder(dashboardTitle);
        if (dashboardUid == null) {
            throw new NotFoundException("Dashboard with title " + dashboardTitle + " not found");
        }

        GrafanaDashboardInfo dashboardInfo = dashboardUid.getFirst();
        String uid = dashboardInfo.getUid();

        GrafanaDetailDashboard detail = grafanaAdapter.getDashboardDetail(AUTHORIZATION + apiKey, uid);

        List<GrafanaResponse> result = new ArrayList<>();

        for (GrafanaPanel panel : detail.getDashboard().getPanels()) {
            String panelTitle = panel.getTitle();

            if ("off".equalsIgnoreCase(filterMap.get(panelTitle))) {
                continue;
            }

            result.add(GrafanaResponse.ofGrafanaResponse(panelTitle, uid));
        }

        return result;
    }

    // 대시보드명으로 패널의 식별자 조회
    public List<GrafanaDashboardResponse> getDashboardPanelInfo(String dashboardName) {
        String uid = getDashboardNameUidByTitle(dashboardName);

        if (uid == null) {
            throw new NotFoundException("Dashboard UID not found for name: " + dashboardName);
        }

        GrafanaDetailDashboard detail = grafanaAdapter.getDashboardDetail(AUTHORIZATION+apiKey, uid);

        List<GrafanaDashboardResponse> responseList = new ArrayList<>();

        List<GrafanaPanel> panels = detail.getDashboard().getPanels();

        for (GrafanaPanel panel : panels) {
            int panelId = panel.getId();
            responseList.add(GrafanaDashboardResponse.ofGrafanaDashboardResponse(panel.getTitle(), uid, panelId));
        }

        return responseList;
    }


    public List<GrafanaDashboardInfo> getDashboard(String uid) {
        return grafanaAdapter.searchDashboards(
                AUTHORIZATION + apiKey,
                uid,
                "dash-db");
    }
}