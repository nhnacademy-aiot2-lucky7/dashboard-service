package com.nhnacademy.dashboard.api;

import com.nhnacademy.common.config.GrafanaApiConfig;
import com.nhnacademy.dashboard.dto.GrafanaDashboard;
import com.nhnacademy.dashboard.dto.GrafanaDashboardInfo;
import com.nhnacademy.dashboard.dto.GrafanaDashboardPanel;
import com.nhnacademy.dashboard.dto.GrafanaFolder;
import com.nhnacademy.dashboard.dto.request.GrafanaCreateDashboardRequest;
import com.nhnacademy.dashboard.dto.response.GrafanaChartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "grafanaAdapter",
        path = "/api",
        url = "http://grafana.luckyseven.live",
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

    // 대시보드의 상세 정보 가져오기
    @GetMapping("/dashboards/uid/{uid}")
    GrafanaDashboardPanel getDashboardDetail(@PathVariable("uid") String uid);

    // 대시보드 응답 전체 가져오기
    @GetMapping("/dashboards/uid/{uid}")
    GrafanaDashboard getDashboardInfo(@PathVariable("uid") String uid);

    // 차트 조회
    @GetMapping("/dashboards/uid/{uid}")
    ResponseEntity<GrafanaDashboardPanel> getChart(
            @PathVariable("uid") String uid);

    // 폴더 안에 있는 대시보드 리스트 가져오기
    @GetMapping("/search")
    List<GrafanaDashboardPanel> getDashboardsByFolder(
            @RequestParam("folderIds") String folderUid,
            @RequestParam("type") String type
    );

    // 차트 생성 및 수정
    @PostMapping("/dashboards/db")
    ResponseEntity<GrafanaChartResponse> createChart(@RequestBody Map<String, Object> dashboardBody);

    @PostMapping("/dashboards/db")
    GrafanaChartResponse updateChart(@RequestBody GrafanaDashboard grafanaDashboard);

}
