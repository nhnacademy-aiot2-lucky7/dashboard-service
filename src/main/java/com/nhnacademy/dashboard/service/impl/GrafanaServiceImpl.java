package com.nhnacademy.dashboard.service.impl;

import com.nhnacademy.dashboard.adapter.GrafanaAdapter;
import com.nhnacademy.dashboard.dto.GrafanaDashboardInfo;
import com.nhnacademy.dashboard.dto.GrafanaFolder;
import com.nhnacademy.dashboard.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GrafanaServiceImpl {

    private static final String AUTHORIZATION = "Bearer ";
    private final GrafanaAdapter grafanaAdapter;
    @Value("${grafana.api-key}")
    private String apiKey;

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
        List<GrafanaDashboardInfo> dashboards = grafanaAdapter.searchDashboards(
                AUTHORIZATION+ apiKey,
                dashboardTitle,
                "dash-db"
        );

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

        List<GrafanaDashboardInfo> dashboards = grafanaAdapter.searchDashboards(
                AUTHORIZATION + apiKey,
                folderUid,
                "dash-db");

        if (dashboards == null || dashboards.isEmpty()) {
            throw new NotFoundException("No dashboards found for folderUid : " + folderUid);
        }

        return dashboards;
    }

    // ifrmeurl 만들기
    public String createIframeUrl(GrafanaDashboardInfo dashboard) {

        if (dashboard == null) {
            throw new NotFoundException("dashboard is NotFound: createIframeUrl");
        }

        long now = System.currentTimeMillis();
        long from = now - (1000 * 60 * 60);

        String iframeUrl = String.format(
                "http://localhost:3000/d-solo/%s/%s?orgId=1&from=%d&to=%d&timezone=browser&panelId=1&__feature.dashboardSceneSolo",
                dashboard.getUid(),
                dashboard.getTitle().replace(" ", "-").toLowerCase(),
                from,
                now
        );

        return String.format(
                "<iframe src=\"%s\" width=\"450\" height=\"200\" frameborder=\"0\"></iframe>",
                iframeUrl
        );
    }
}