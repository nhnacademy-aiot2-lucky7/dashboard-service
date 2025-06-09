package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.dto.dashboard.CreateDashboardRequest;
import com.nhnacademy.dashboard.dto.dashboard.DeleteDashboardRequest;
import com.nhnacademy.dashboard.dto.dashboard.InfoDashboardResponse;
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
        List<InfoDashboardResponse> dashboards = grafanaDashboardService.getDashboard(userId);

        List<String> result = dashboards.stream()
                .map(InfoDashboardResponse::getDashboardTitle)
                .toList();

        return ResponseEntity.ok(result);
    }

    /**
     * 사용자 ID를 기반으로 모든 대시보드 정보를 조회합니다.
     *
     * @param userId 요청자의 사용자 ID (헤더 X-User-Id)
     * @return 전체 대시보드 정보 목록을 반환합니다.
     */
    @GetMapping("/user")
    @Operation(summary ="모든 대시보드 조회")
    public ResponseEntity<List<InfoDashboardResponse>> getDashboard(@RequestHeader("X-User-Id") String userId) {

        List<InfoDashboardResponse> result = grafanaDashboardService.getDashboard(userId);
        return ResponseEntity.ok(result);
    }

    /**
     * 선택된 부서명을 기반으로 모든 대시보드 정보를 조회합니다.
     *
     * @param folderTitle 부서 목록을 가져옵니다.
     * @return 전체 대시보드 정보 목록을 반환합니다.
     */
    @GetMapping("/admin")
    @Operation(summary ="모든 대시보드 조회")
    public ResponseEntity<List<InfoDashboardResponse>> getAdminDashboard(@RequestParam(required = false) String folderTitle) {

        List<InfoDashboardResponse> result = grafanaDashboardService.getAdminDashboard(folderTitle);
        return ResponseEntity.ok(result);
    }

    /**
     * 사용자 ID를 기반으로 모든 대시보드 정보를 조회합니다.
     *
     * @return 전체 대시보드 정보 목록을 반환합니다.
     */
    @GetMapping("/all")
    @Operation(summary ="모든 대시보드 조회")
    public ResponseEntity<List<InfoDashboardResponse>> getAllDashboard() {

        List<InfoDashboardResponse> result = grafanaDashboardService.getAllDashboard();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Operation(summary = "대시보드 생성")
    public ResponseEntity<Void> createDashboard(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody CreateDashboardRequest createDashboardRequest) {

        grafanaDashboardService.createDashboard(userId, createDashboardRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @PutMapping
    @Operation(summary = "대시보드 이름 수정")
    public ResponseEntity<Void> updateDashboard(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody UpdateDashboardNameRequest updateDashboardNameRequest
    ) {
        grafanaDashboardService.updateDashboardName(userId,updateDashboardNameRequest);
        return ResponseEntity
                .ok().build();
    }

    @DeleteMapping
    @Operation(summary = "대시보드 삭제")
    public ResponseEntity<Void> deleteDashboard(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody DeleteDashboardRequest deleteDashboardRequest){
        grafanaDashboardService.removeDashboard(userId, deleteDashboardRequest);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
