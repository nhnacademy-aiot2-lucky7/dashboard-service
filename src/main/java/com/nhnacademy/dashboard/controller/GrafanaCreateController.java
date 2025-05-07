package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.dto.front_dto.create.CreateDashboardRequest;
import com.nhnacademy.dashboard.dto.front_dto.create.CreatePanelRequest;
import com.nhnacademy.dashboard.service.CreateService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GrafanaCreateController {

    private final CreateService createService;

    @PostMapping("/folders")
    @Operation(summary = "모든 폴더 생성")
    public ResponseEntity<Void> createAllFolder(
    ){
        createService.createAllFolder();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @PostMapping("/folder")
    @Operation(summary = "새로운 폴더 생성")
    public ResponseEntity<Void> createFolder(
            @RequestHeader("X-UserId") String userId
    ){
        createService.createFolder(userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @PostMapping("/dashboards")
    @Operation(summary = "새로운 대시보드 추가")
    public ResponseEntity<Void> createDashboard(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody CreateDashboardRequest createDashboardRequest) {

        createService.createDashboard(userId, createDashboardRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @PostMapping("/charts")
    @Operation(summary = "새로운 차트 추가")
    public ResponseEntity<Void> createChart(
            @RequestHeader ("X-User-Id") String userId,
            @RequestBody CreatePanelRequest createPanelRequest
    ) {

        createService.createChart(userId, createPanelRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}
