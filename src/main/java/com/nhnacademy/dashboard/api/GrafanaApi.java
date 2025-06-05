package com.nhnacademy.dashboard.api;

import com.nhnacademy.common.config.FeignConfig;
import com.nhnacademy.common.config.GrafanaApiConfig;
import com.nhnacademy.dashboard.dto.dashboard.DataSourceResponse;
import com.nhnacademy.dashboard.dto.folder.CreateFolderRequest;
import com.nhnacademy.dashboard.dto.folder.GrafanaUpdateFolderRequest;
import com.nhnacademy.dashboard.dto.grafana.GrafanaMetaResponse;
import com.nhnacademy.dashboard.dto.dashboard.InfoDashboardResponse;
import com.nhnacademy.dashboard.dto.folder.FolderInfoResponse;
import com.nhnacademy.dashboard.dto.dashboard.GrafanaCreateDashboardRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Grafana API와 통신하기 위한 Feign Client입니다.
 */
@FeignClient(
        name = "grafana",
        url = "${grafana.api.url}",
        path = "/api",
        configuration = {FeignConfig.class, GrafanaApiConfig.class}
)
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
     * 차트 생성 맟 수정합니다.
     *
     * @param dashboardBody 차트 생성 요청 데이터
     * @return 생성된 차트 응답
     */
    @PostMapping("/dashboards/db")
    ResponseEntity<GrafanaMetaResponse> updateDashboard(@RequestBody GrafanaCreateDashboardRequest dashboardBody);

    /**
     * 모든 폴더 목록을 조회합니다.
     *
     * @return 폴더 리스트
     */
    @GetMapping(value = "/folders")
    List<FolderInfoResponse> getAllFolders();

    /**
     * 폴더 이름을 수정합니다.
     *
     * @param request 수정할 폴더 요청 정보
     * @return 생성 결과 응답 (Body 없음)
     */
    @PutMapping("/folders/{uid}")
    ResponseEntity<Void> updateFolder(@PathVariable String uid, @RequestBody GrafanaUpdateFolderRequest request);

    /**
     * 모든 폴더 목록을 생성합니다.
     *
     * @return 폴더 리스트
     */
    @PostMapping(value = "/folders")
    ResponseEntity<Void> createFolder(@RequestBody CreateFolderRequest createFolderRequest);

    /**
     * 폴더 ID와 타입을 기반으로 대시보드를 검색합니다.
     *
     * @param folderId 폴더 ID
     * @param type 검색 타입 (예: "dash-db")
     * @return 대시보드 정보 리스트
     */
    @GetMapping("/search")
    List<InfoDashboardResponse> searchDashboards(
            @RequestParam("folderIds") List<Integer> folderId,
            @RequestParam("type") String type
    );

    /**
     * InfluxDB의 datasource를 검색합니다.
     *
     * @return DataSourceResponse
     */
    @GetMapping("/datasources")
    List<DataSourceResponse> getDataSource();

    /**
     * UID를 통해 대시보드 전체 정보를 가져옵니다.
     *
     * @param uid 대시보드 UID
     * @return 대시보드 정보
     */
    @GetMapping("/dashboards/uid/{uid}")
    GrafanaCreateDashboardRequest getDashboardInfo(@PathVariable("uid") String uid);

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