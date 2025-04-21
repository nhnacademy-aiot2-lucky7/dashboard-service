package com.nhnacademy.dashboard.adapter;

import com.nhnacademy.dashboard.dto.GrafanaDashboardInfo;
import com.nhnacademy.dashboard.dto.GrafanaFolder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
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


}
