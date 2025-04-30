package com.nhnacademy.dashboard.api;

import com.nhnacademy.common.config.GrafanaApiConfig;
import com.nhnacademy.dashboard.dto.GrafanaDashboard;
import com.nhnacademy.dashboard.dto.GrafanaDashboardInfo;
import com.nhnacademy.dashboard.dto.GrafanaDashboardPanel;
import com.nhnacademy.dashboard.dto.GrafanaFolder;
import com.nhnacademy.dashboard.dto.request.GrafanaCreateDashboardRequest;
import com.nhnacademy.dashboard.dto.response.GrafanaDashboardResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Grafana API와 통신하기 위한 Feign Client입니다.
 */
@FeignClient(
        name = "grafanaAdapter",
        path = "/api",
        url = "http://grafana.luckyseven.live",
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
    List<GrafanaFolder> getAllFolders();

    /**
     * 폴더 ID와 타입을 기반으로 대시보드를 검색합니다.
     *
     * @param folderId 폴더 ID
     * @param type 검색 타입 (예: "dash-db")
     * @return 대시보드 정보 리스트
     */
    @GetMapping("/search")
    List<GrafanaDashboardInfo> searchDashboards(
            @RequestParam("folderIds") int folderId,
            @RequestParam("type") String type
    );

    /**
     * UID를 통해 특정 대시보드의 상세 정보를 가져옵니다.
     *
     * @param uid 대시보드 UID
     * @return 대시보드 패널 정보
     */
    @GetMapping("/dashboards/uid/{uid}")
    GrafanaDashboardPanel getDashboardDetail(@PathVariable("uid") String uid);

    /**
     * UID를 통해 대시보드 전체 정보를 가져옵니다.
     *
     * @param uid 대시보드 UID
     * @return 대시보드 정보
     */
    @GetMapping("/dashboards/uid/{uid}")
    GrafanaDashboard getDashboardInfo(@PathVariable("uid") String uid);

    /**
     * UID를 통해 대시보드 차트를 조회합니다.
     *
     * @param uid 대시보드 UID
     * @return 대시보드 패널 응답
     */
    @GetMapping("/dashboards/uid/{uid}")
    ResponseEntity<GrafanaDashboardPanel> getChart(@PathVariable("uid") String uid);

    /**
     * 폴더 ID와 타입을 기반으로 폴더 안에 있는 대시보드 리스트를 가져옵니다.
     *
     * @param folderUid 폴더 UID
     * @param type 검색 타입 (예: "dash-db")
     * @return 대시보드 패널 리스트
     */
    @GetMapping("/search")
    List<GrafanaDashboardPanel> getDashboardsByFolder(
            @RequestParam("folderIds") String folderUid,
            @RequestParam("type") String type
    );

    /**
     * 차트 생성 맟 수정합니다.
     *
     * @param dashboardBody 차트 생성 요청 데이터
     * @return 생성된 차트 응답
     */
    @PostMapping("/dashboards/db")
    ResponseEntity<GrafanaDashboardResponse> createChart(@RequestBody GrafanaDashboard dashboardBody);

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