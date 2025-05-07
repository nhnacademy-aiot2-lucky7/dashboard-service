package com.nhnacademy.dashboard.api;

import com.nhnacademy.common.config.GrafanaApiConfig;
import com.nhnacademy.dashboard.dto.front_dto.create.CreateFolderRequest;
import com.nhnacademy.dashboard.dto.grafana_dto.GrafanaResponse;
import com.nhnacademy.dashboard.dto.grafana_dto.JsonGrafanaDashboardRequest;
import com.nhnacademy.dashboard.dto.front_dto.response.DashboardInfoResponse;
import com.nhnacademy.dashboard.dto.front_dto.response.FolderInfoResponse;
import com.nhnacademy.dashboard.dto.grafana_dto.GrafanaCreateDashboardRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Grafana API와 통신하기 위한 Feign Client입니다.
 */
@FeignClient(
        name = "GRAFANA-SERVICE",
        path = "/api",
        configuration = GrafanaApiConfig.class)
public interface GrafanaApi {

    /**
     * 대시보드를 생성합니다.
     *
     * @param request 생성할 대시보드 요청 정보
     * @return 생성 결과 응답 (Body 없음)
     */
    @PostMapping("/dashboards/db")
    ResponseEntity<Void> createDashboard(@RequestBody GrafanaCreateDashboardRequest request);

    /**
     * 모든 폴더 목록을 조회합니다.
     *
     * @return 폴더 리스트
     */
    @GetMapping(value = "/folders")
    List<FolderInfoResponse> getAllFolders();

    /**
     * 모든 폴더 목록을 생성합니다.
     *
     * @return 폴더 리스트
     */
    @PostMapping(value = "/folders")
    ResponseEntity<List<FolderInfoResponse>> createAllFolder(@RequestBody List<CreateFolderRequest> createFolderRequest);


    /**
     * 모든 폴더 목록을 생성합니다.
     *
     * @return 폴더 리스트
     */
    @PostMapping(value = "/folders")
    ResponseEntity<FolderInfoResponse> createFolder(@RequestBody CreateFolderRequest createFolderRequest);

    /**
     * 폴더 ID와 타입을 기반으로 대시보드를 검색합니다.
     *
     * @param folderId 폴더 ID
     * @param type 검색 타입 (예: "dash-db")
     * @return 대시보드 정보 리스트
     */
    @GetMapping("/search")
    List<DashboardInfoResponse> searchDashboards(
            @RequestParam("folderIds") int folderId,
            @RequestParam("type") String type
    );

    /**
     * UID를 통해 대시보드 전체 정보를 가져옵니다.
     *
     * @param uid 대시보드 UID
     * @return 대시보드 정보
     */
    @GetMapping("/dashboards/uid/{uid}")
    JsonGrafanaDashboardRequest getDashboardInfo(@PathVariable("uid") String uid);


    /**
     * 차트 생성 맟 수정합니다.
     *
     * @param dashboardBody 차트 생성 요청 데이터
     * @return 생성된 차트 응답
     */
    @PostMapping("/dashboards/db")
    ResponseEntity<GrafanaResponse> createChart(@RequestBody JsonGrafanaDashboardRequest dashboardBody);

    /**
     * 주어진 UID에 해당하는 폴더를 삭제하는 API입니다.
     * <p>
     * 요청 경로에서 폴더의 UID를 받아 해당 폴더를 삭제합니다.
     * </p>
     *
     * @param uid 삭제할 폴더의 UID
     * @return HTTP 상태 코드 204(No Content)와 함께 응답
     */
    @DeleteMapping("/folders/{uid}")
    ResponseEntity<Void> deleteFolder(@PathVariable("uid") String uid);

    /**
     * 주어진 UID에 해당하는 대시보드를 삭제하는 API입니다.
     * <p>
     * 요청 경로에서 대시보드의 UID를 받아 해당 대시보드를 삭제합니다.
     * </p>
     *
     * @param uid 삭제할 대시보드의 UID
     * @return HTTP 상태 코드 204(No Content)와 함께 응답
     */
    @DeleteMapping("/dashboards/uid/{uid}")
    ResponseEntity<Void> deleteDashboard(@PathVariable("uid") String uid);
}