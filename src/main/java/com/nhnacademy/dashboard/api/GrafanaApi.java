package com.nhnacademy.dashboard.api;

import com.nhnacademy.common.config.GrafanaApiConfig;
import com.nhnacademy.dashboard.dto.GrafanaDashboardInfo;
import com.nhnacademy.dashboard.dto.GrafanaDashboardPanel;
import com.nhnacademy.dashboard.dto.GrafanaDashboardResponse;
import com.nhnacademy.dashboard.dto.GrafanaFolder;
import com.nhnacademy.dashboard.request.GrafanaCreateDashboardRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "grafanaAdapter",
        path = "/api",
        url = "http://localhost:3000",
        configuration = GrafanaApiConfig.class)
public interface GrafanaApi {

    // 대시보드 생성
    @PostMapping("/dashboards/db")
    ResponseEntity<Void> createDashboard(@RequestBody GrafanaCreateDashboardRequest request);

    // 폴더 조회
    @GetMapping(value = "/folders")
    List<GrafanaFolder> getAllFolders();

    // 폴더/대시보드 조회
    @GetMapping("/search")
    List<GrafanaDashboardInfo> searchDashboards(
            @RequestParam("folderIds") int folderId,
            @RequestParam("type") String type
    );

    // 🌟대시보드의 상세 정보 가져오기
    @GetMapping("/dashboards/uid/{uid}")
    GrafanaDashboardPanel getDashboardDetail(@PathVariable("uid") String uid);

    // 🌟차트 조회
    @GetMapping("/dashboards/uid/{uid}")
    ResponseEntity<GrafanaDashboardPanel> getChart(
            @PathVariable("uid") String uid);

    // 🌟폴더 안에 있는 대시보드 리스트 가져오기
    @GetMapping("/search")
    List<GrafanaDashboardPanel> getDashboardsByFolder(
            @RequestParam("folderIds") String folderUid,
            @RequestParam("type") String type
    );

}
