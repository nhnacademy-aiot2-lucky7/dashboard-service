package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.dto.GrafanaDashboardInfo;
import com.nhnacademy.dashboard.dto.GrafanaDashboardResponse;
import com.nhnacademy.dashboard.dto.GrafanaFolder;
import com.nhnacademy.dashboard.dto.GrafanaResponse;
import com.nhnacademy.dashboard.exception.NotFoundException;
import com.nhnacademy.dashboard.service.impl.GrafanaServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GrafanaController {

    private final GrafanaServiceImpl grafanaService;

    @GetMapping("/folders")
    @Operation(summary = "모든 폴더 조회")
    public List<GrafanaFolder> getFolders(){
        List<GrafanaFolder> response = grafanaService.getAllFolders();
        if(response.isEmpty()){
            throw new NotFoundException("getFolders is not Found");
        }

        return response;
    }

    @GetMapping("/folders/{title}")
    @Operation(summary ="폴더명으로 대시보드명 조회")
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

    @GetMapping(value = "/folders/{title}/iframes")
    @Operation(summary ="폴더명으로 모든 대시보드 조회")
    public List<GrafanaResponse> getIframeUrlsToFolder(@PathVariable String title) {

        String folderUid = grafanaService.getFolderUidByTitle(title);

        if (folderUid == null) {
            throw new NotFoundException("folderUid is null: getIframeUrlsToFolder");
        }

        List<GrafanaDashboardInfo> dashboards = grafanaService.getDashboardsInFolder(folderUid);

        log.info("getIframeUrlsToFolder -> dashboards: {}", dashboards);
        return dashboards.stream()
                .filter(d -> folderUid.equals(d.getFolderUid()))
                .map(d -> GrafanaResponse.ofGrafanaResponse(d.getTitle(), d.getUid()))
                .toList();
    }

    @GetMapping(value = "/{dashboardName}/iframes")
    @Operation(summary ="대시보드명으로 차트 조회")
    public ResponseEntity<List<GrafanaDashboardResponse>> getIframeUrlsToName(@PathVariable String dashboardName) {

        List<GrafanaDashboardResponse> responses = grafanaService.getDashboardPanelInfo(dashboardName);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/chart")
    @Operation(summary ="메인페이지 on/off 필터 조회")
    public ResponseEntity<List<GrafanaResponse>> getDashboardCharts(
            @RequestParam String dashboardTitle,
            @RequestParam(name = "filter", required = false) String filter
    ) {
        Map<String, String> filterMap = grafanaService.parseFilter(filter);
        List<GrafanaResponse> charts  = grafanaService.getDashboardCharts(dashboardTitle, filterMap);
        return ResponseEntity.ok(charts);
    }

}
