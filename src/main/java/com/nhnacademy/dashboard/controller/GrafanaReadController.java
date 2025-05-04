package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.dto.frontdto.read.ReadPanelRequest;
import com.nhnacademy.dashboard.dto.frontdto.read.ReadFilterChartRequest;
import com.nhnacademy.dashboard.dto.frontdto.response.DashboardInfoResponse;
import com.nhnacademy.dashboard.dto.frontdto.response.FolderInfoResponse;
import com.nhnacademy.dashboard.dto.frontdto.response.IframePanelResponse;
import com.nhnacademy.dashboard.exception.NotFoundException;
import com.nhnacademy.dashboard.service.impl.GrafanaServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GrafanaReadController {

    private final GrafanaServiceImpl grafanaService;
    /**
     * 모든 폴더를 조회하는 API.
     *
     * @return 모든 폴더 목록
     * @throws NotFoundException 폴더가 없을 경우 예외 발생
     */
    @GetMapping("/folders")
    @Operation(summary = "모든 폴더 조회")
    public ResponseEntity<List<FolderInfoResponse>> getFolders(){

        List<FolderInfoResponse> result = grafanaService.getAllFolders();
        return ResponseEntity.ok(result);
    }

    /**
     * 모든 대시보드의 이름을 조회합니다.
     *
     * @param userId 요청자의 사용자 ID (헤더 X-User-Id)
     * @return 대시보드 이름 목록을 반환합니다.
     */
    @GetMapping("/dashboardsName")
    @Operation(summary ="모든 대시보드 이름 조회")
    public ResponseEntity<List<String>> getDashboardName(@RequestHeader("X-User-Id") String userId) {
        List<DashboardInfoResponse> dashboards = grafanaService.getDashboard(userId);

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
    @GetMapping(value = "/dashboards")
    @Operation(summary ="모든 대시보드 조회")
    public ResponseEntity<List<DashboardInfoResponse>> getAllDashboard(@RequestHeader("X-User-Id") String userId) {

        List<DashboardInfoResponse> result = grafanaService.getDashboard(userId);
        return ResponseEntity.ok(result);
    }


    /**
     * 요청으로 전달된 대시보드 UID 및 제목을 기반으로 해당 차트들을 조회합니다.
     *
     * @param readPanelRequest 대시보드 UID와 제목 정보가 담긴 요청 객체
     * @return 해당 대시보드에 포함된 차트 목록을 반환합니다.
     */
    @GetMapping(value = "/dashboards/charts")
    @Operation(summary = "차트 조회")
    public ResponseEntity<List<IframePanelResponse>> getChartByName(
            @RequestBody ReadPanelRequest readPanelRequest) {

        List<IframePanelResponse> result = grafanaService.getChart(readPanelRequest);
        return ResponseEntity.ok(result);
    }


    /**
     * 대시보드 UID 및 on/off 필터 값을 기준으로 필터링된 차트를 조회합니다.
     *
     * @param readFilterChartRequest 대시보드 UID 및 필터 조건이 포함된 요청 객체
     * @return 필터 조건에 부합하는 차트 목록을 반환합니다.
     */
    @GetMapping("/dashboards/filtered-chart")
    @Operation(summary ="메인페이지 on/off 필터 조회")
    public ResponseEntity<List<IframePanelResponse>> getDashboardCharts(
            @RequestBody ReadFilterChartRequest readFilterChartRequest
    ) {
        Map<String, String> filterMap = grafanaService.parseFilter(readFilterChartRequest.getFilter());
        List<IframePanelResponse> charts  = grafanaService.getFilterCharts(readFilterChartRequest.getDashboardUid(), filterMap);
        return ResponseEntity.ok(charts);
    }
}
