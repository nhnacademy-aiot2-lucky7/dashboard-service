package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.dto.response.GrafanaResponse;
import com.nhnacademy.dashboard.service.impl.GrafanaServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GrafanaDeleteController {
    private final GrafanaServiceImpl grafanaService;


    @DeleteMapping("/folder")
    @Operation(summary = "폴더 삭제")
    public ResponseEntity<Void> deleteFolder(@RequestHeader String id){
        grafanaService.removeFolder(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @DeleteMapping("/dashboard")
    @Operation(summary = "대시보드 삭제")
    public ResponseEntity<Void> deleteDashboard(@RequestBody String dashboardUid){
        grafanaService.removeDashboard(dashboardUid);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/chart")
    @Operation(summary = "차트 삭제")
    public ResponseEntity<GrafanaResponse> deleteChart(
            @RequestBody String dashboardUid,
            @RequestBody String chartTitle){
        GrafanaResponse response = grafanaService.removeChart(dashboardUid, chartTitle);

        return ResponseEntity.ok(response);
    }
}
