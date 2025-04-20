package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.adapter.GrafanaAdapter;
import com.nhnacademy.dashboard.dto.GrafanaDashboardInfo;
import com.nhnacademy.dashboard.dto.GrafanaFolder;
import com.nhnacademy.dashboard.service.GrafanaServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
public class GrafanaController {

    Logger log = LoggerFactory.getLogger(getClass());

    private final GrafanaAdapter grafanaAdapter;

    private final GrafanaServiceImpl grafanaService;

    @Value("${grafana.api-key}")
    private String apiKey;

    public GrafanaController(GrafanaAdapter grafanaAdapter, GrafanaServiceImpl grafanaService) {
        this.grafanaAdapter = grafanaAdapter;
        this.grafanaService = grafanaService;
    }

    @GetMapping("/folders")
    public List<GrafanaFolder> getFolders(){
        List<GrafanaFolder> response =grafanaAdapter.getAllFolders("Bearer " + apiKey);

        List<GrafanaFolder> filtered = response.stream()
                .filter(folder -> folder.getId() >= 0).toList();

        log.info("필터링된 response: {}", filtered);
        return filtered;
    }

    @GetMapping("/folders/{title}/iframes")
    public List<String> getIframeUrls(@PathVariable String title) {

        String folderUid = grafanaService.getFolderUidByTitle(title);

        if (folderUid == null) {
            return Collections.emptyList();
        }

        List<GrafanaDashboardInfo> dashboards = grafanaService.getDashboardsInFolder(folderUid);

        return dashboards.stream()
                .map(this::createIframeUrl).toList();  // 대시보드별 iframe URL 생성
    }

    private String createIframeUrl(GrafanaDashboardInfo dashboard) {
        String iframeUrl = String.format(
                "http://localhost:3000/d-solo/%s/%s?orgId=1&from=1745115501466&to=1745137101466&timezone=browser&panelId=1&__feature.dashboardSceneSolo",
                dashboard.getUid(),
                dashboard.getTitle()
        );

        // iframe HTML 태그 형식으로 반환
        return "<iframe src=\"" + iframeUrl + "\" width=\"450\" height=\"200\" frameborder=\"0\"></iframe>";

    }

}
