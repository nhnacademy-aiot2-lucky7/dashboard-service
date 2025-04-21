package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.dto.GrafanaDashboardInfo;
import com.nhnacademy.dashboard.dto.GrafanaFolder;
import com.nhnacademy.dashboard.dto.GrafanaResponse;
import com.nhnacademy.dashboard.exception.NotFoundException;
import com.nhnacademy.dashboard.service.impl.GrafanaServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GrafanaController {

    private final GrafanaServiceImpl grafanaService;

    // 모든 폴더 조회
    @GetMapping("/folders")
    public List<GrafanaFolder> getFolders(){
        List<GrafanaFolder> response = grafanaService.getAllFolders();
        if(response.isEmpty()){
            throw new NotFoundException("getFolders is not Found");
        }

        return response;
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

    // 폴더명으로 모든 대시보드 조회
    @GetMapping(value = "/folders/{title}/iframes")
    public List<GrafanaResponse> getIframeUrlsToFolder(@PathVariable String title) {

        String folderUid = grafanaService.getFolderUidByTitle(title);

        if (folderUid == null) {
            throw new NotFoundException("folderUid is null: getIframeUrlsToFolder");
        }

        List<GrafanaDashboardInfo> dashboards = grafanaService.getDashboardsInFolder(folderUid);

        log.info("getIframeUrlsToFolder -> dashboards: {}", dashboards);
        return dashboards.stream()
                .filter(d -> folderUid.equals(d.getFolderUid()))
                .map(d -> new GrafanaResponse(d.getTitle(), d.getUid()))
                .toList();
    }

    // 대시보드명으로 특정 대시보드 조회
    @GetMapping(value = "/{name}/iframes")
    public GrafanaResponse getIframeUrlsToName(@PathVariable String name) {

        String dashboardUid = grafanaService.getDashboardNameUidByTitle(name);

        if (dashboardUid == null) {
            throw new NotFoundException("folderUid is null: getIframeUrlsToName");
        }

        return new GrafanaResponse(name, dashboardUid);
    }
}
