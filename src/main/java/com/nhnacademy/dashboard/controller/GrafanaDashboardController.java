package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.dto.dashboard.CreateDashboardRequest;
import com.nhnacademy.dashboard.dto.dashboard.DeleteDashboardRequest;
import com.nhnacademy.dashboard.dto.dashboard.DashboardInfoResponse;
import com.nhnacademy.dashboard.dto.dashboard.UpdateDashboardNameRequest;
import com.nhnacademy.dashboard.service.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dashboards")
@RequiredArgsConstructor
public class GrafanaDashboardController {

    private final GrafanaDashboardService grafanaDashboardService;

    /**
     * 모든 대시보드의 이름을 조회합니다.
     *
     * @param userId 요청자의 사용자 ID (헤더 X-User-Id)
     * @return 대시보드 이름 목록을 반환합니다.
     */
    @GetMapping("/names")
    @Operation(summary ="모든 대시보드 이름 조회")
    public ResponseEntity<List<String>> getDashboardName(@RequestHeader("X-User-Id") String userId) {
        List<DashboardInfoResponse> dashboards = grafanaDashboardService.getDashboard(userId);

        List<String> result = dashboards.stream()
                .map(DashboardInfoResponse::getDashboardTitle)
                .toList();

        return ResponseEntity.ok(result);
    }

    /**
     * 사용자 ID를 기반으로 모든 대시보드 정보를 조회합니다.
     *
     * @param userId 요청자의 사용자 ID (헤더 X-User-Id)
     * @return 전체 대시보드 정보 목록을 반환합니다.
     */
    @GetMapping
    @Operation(summary ="모든 대시보드 조회")
    public ResponseEntity<List<DashboardInfoResponse>> getAllDashboard(@RequestHeader("X-User-Id") String userId) {

        List<DashboardInfoResponse> result = grafanaDashboardService.getDashboard(userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Operation(summary = "새로운 대시보드 추가")
    public ResponseEntity<Void> createDashboard(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody CreateDashboardRequest createDashboardRequest) {

        grafanaDashboardService.createDashboard(userId, createDashboardRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @PutMapping("/name")
    @Operation(summary = "대시보드 이름 수정")
    public ResponseEntity<Void> updateDashboard(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody UpdateDashboardNameRequest updateDashboardNameRequest
    ){
        grafanaDashboardService.updateDashboardName(userId, updateDashboardNameRequest);
        return ResponseEntity
                .ok().build();
    }

    @DeleteMapping
    @Operation(summary = "대시보드 삭제")
    public ResponseEntity<Void> deleteDashboard(@RequestBody DeleteDashboardRequest deleteDashboardRequest){
        grafanaDashboardService.removeDashboard(deleteDashboardRequest);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
