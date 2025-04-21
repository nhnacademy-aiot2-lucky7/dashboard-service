package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.adapter.GrafanaAdapter;
import com.nhnacademy.dashboard.dto.GrafanaDashboardInfo;
import com.nhnacademy.dashboard.dto.GrafanaFolder;
import com.nhnacademy.dashboard.exception.NotFoundException;
import com.nhnacademy.dashboard.service.impl.GrafanaServiceImpl;
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

    // 모든 폴더 조회
    @GetMapping("/folders")
    public List<GrafanaFolder> getFolders(){
        List<GrafanaFolder> response =grafanaAdapter.getAllFolders("Bearer " + apiKey);

        List<GrafanaFolder> filtered = response.stream()
                .filter(folder -> folder.getId() >= 0).toList();

        log.info("필터링된 response: {}", filtered);
        return filtered;
    }

    // 폴더명으로 대시보드명 조회
    @GetMapping("/folders/{title}")
    public List<String> getDashboardName(@PathVariable String title) {
        String folderUid = grafanaService.getFolderUidByTitle(title);

        if (folderUid == null) {
            return Collections.emptyList();
        }

        List<GrafanaDashboardInfo> dashboards = grafanaService.getDashboardsInFolder(folderUid);

        log.info("getDashboardName-> dashboards: {}", dashboards);

        return dashboards.stream()
                .filter(d -> folderUid.equals(d.getFolderUid()))
                .map(GrafanaDashboardInfo::getTitle)
                .toList();
    }

    // 폴더명으로 모든 대시보드 iframe 반환
    @GetMapping(value = "/folders/{title}/iframes")
    public List<String> getIframeUrlsToFolder(@PathVariable String title) {

        String folderUid = grafanaService.getFolderUidByTitle(title);

        if (folderUid == null) {
            throw new NotFoundException("folderUid is null: getIframeUrlsToFolder");
        }

        List<GrafanaDashboardInfo> dashboards = grafanaService.getDashboardsInFolder(folderUid);

        log.info("getIframeUrlsToFolder -> dashboards: {}", dashboards);
        return dashboards.stream()
                .filter(d -> folderUid.equals(d.getFolderUid()))
                .map(grafanaService::createIframeUrl)
                .toList();
    }

    // 대시보드명으로 특정 대시보드 iframe 반환
    @GetMapping(value = "/{name}/iframes")
    public List<String> getIframeUrlsToName(@PathVariable String name) {

        String dashboardUid = grafanaService.getDashboardNameUidByTitle(name);

        if (dashboardUid == null) {
            throw new NotFoundException("folderUid is null: getIframeUrlsToName");
        }

        List<GrafanaDashboardInfo> dashboards = grafanaService.getDashboardsInFolder(dashboardUid);

        return dashboards.stream()
                .filter(d -> dashboardUid.equals(d.getUid()))
                .map(grafanaService::createIframeUrl)
                .toList();
    }
}
