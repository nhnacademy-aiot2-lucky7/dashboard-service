package com.nhnacademy.dashboard.adapter;

import com.nhnacademy.dashboard.dto.GrafanaDashboardInfo;
import com.nhnacademy.dashboard.dto.GrafanaDetailDashboard;
import com.nhnacademy.dashboard.dto.GrafanaFolder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "grafanaAdapter", path = "/api", url = "http://localhost:3000")
public interface GrafanaAdapter {

    @GetMapping(value = "/folders")
    List<GrafanaFolder> getAllFolders(@RequestHeader("Authorization") String authorization);

    @GetMapping("/search")
    List<GrafanaDashboardInfo> searchDashboards(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("folderIds") String folderUid,
            @RequestParam("type") String type
    );

    // 대시보드의 상세 정보 가져오기 (패널 포함)
    @GetMapping("/dashboards/uid/{uid}")
    GrafanaDetailDashboard getDashboardDetail(@RequestHeader("Authorization") String authorization,
                                              @PathVariable("uid") String uid);

    // 폴더 안에 있는 대시보드 리스트 가져오기
    @GetMapping("/search")
    List<GrafanaDetailDashboard> getDashboardsByFolder(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("folderIds") String folderUid,
            @RequestParam("type") String type
    );

}
