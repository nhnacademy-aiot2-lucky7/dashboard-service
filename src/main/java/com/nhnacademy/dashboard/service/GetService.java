package com.nhnacademy.dashboard.service;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.api.UserApi;
import com.nhnacademy.dashboard.dto.front_dto.response.DashboardInfoResponse;
import com.nhnacademy.dashboard.dto.front_dto.response.FolderInfoResponse;
import com.nhnacademy.dashboard.dto.front_dto.response.IframePanelResponse;
import com.nhnacademy.dashboard.dto.front_dto.read.ReadPanelRequest;
import com.nhnacademy.dashboard.dto.grafana_dto.*;
import com.nhnacademy.dashboard.dto.grafana_dto.dashboard_dto.*;
import com.nhnacademy.dashboard.dto.user_dto.UserDepartmentResponse;
import com.nhnacademy.dashboard.dto.user_dto.UserInfoResponse;
import com.nhnacademy.dashboard.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetService {

    private final GrafanaApi grafanaApi;
    public static final String TYPE = "dash-db";
    private static final String INFLUXDB_UID = "o4aKnEJNk";
    private final UserApi userApi;


    /**
     * 사용자 ID를 기반으로 해당 사용자의 부서명을 반환합니다.
     *
     * @param userId 사용자 ID
     * @return 부서 이름
     * @throws NotFoundException 사용자가 없거나 부서가 없을 경우
     */
    public String getFolderTitle(String userId){
        UserInfoResponse userInfoResponse = userApi.getDepartmentId(userId).getBody();

        if(userInfoResponse == null){
            throw new NotFoundException("user 찾을 수 없습니다: "+userId);
        }
        String departmentId = userInfoResponse.getUserDepartment();
        UserDepartmentResponse userDepartmentResponse = userApi.getDepartmentName(departmentId).getBody();

        if(userDepartmentResponse == null){
            throw new NotFoundException("department 찾을 수 없습니다: "+departmentId);
        }
        return userDepartmentResponse.getDepartmentName();
    }

    /**
     * 주어진 대시보드 제목에 해당하는 대시보드 정보를 반환합니다.
     *
     * @param userId 사용자 ID
     * @param dashboardTitle 대시보드 제목
     * @return 대시보드 정보
     * @throws NotFoundException 해당 제목의 대시보드가 없을 경우
     */
    public DashboardInfoResponse getDashboardInfoRequest(String userId, String dashboardTitle){
        List<DashboardInfoResponse> dashboardInfoResponseList = getDashboard(userId);
        return dashboardInfoResponseList.stream()
                .filter(d -> d.getDashboardTitle().equals(dashboardTitle))
                .findFirst()
                .orElseThrow(()->new NotFoundException("대시보드제목을 찾을 수 없습니다: "+dashboardTitle));
    }



    /**
     * Grafana에 등록된 모든 폴더를 조회합니다.
     *
     * @return 폴더 정보 리스트
     */
    public List<FolderInfoResponse> getAllFolders() {
        List<FolderInfoResponse> folders = grafanaApi.getAllFolders();

        log.info("전체 폴더: {}", folders.toString());
        return folders;
    }


    /**
     * 사용자 ID를 기반으로 사용자의 부서 폴더에 포함된 대시보드 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 대시보드 목록
     */
    public List<DashboardInfoResponse> getDashboard(String userId) {

        String folderTitle = getFolderTitle(userId);
        List<DashboardInfoResponse> dashboards = grafanaApi.searchDashboards(getFolderIdByTitle(folderTitle), TYPE);
        log.info("getDashboardByTitle -> dashboards: {}", dashboards);
        return dashboards;
    }

    /**
     * 특정 대시보드 UID에 해당하는 모든 패널 정보를 Iframe 형식으로 반환합니다.
     *
     * @param readPanelRequest 대시보드 UID를 포함한 요청
     * @return Iframe 패널 응답 목록
     * @throws NotFoundException 대시보드 UID가 존재하지 않을 경우
     */

    public List<IframePanelResponse> getChart(ReadPanelRequest readPanelRequest) {

        JsonGrafanaDashboardRequest dashboard = grafanaApi.getDashboardInfo(readPanelRequest.getDashboardUid());
        if (dashboard == null) {
            throw new NotFoundException("존재하지 않는 uid : "+ readPanelRequest.getDashboardUid());
        }

        List<Panel> panels = dashboard.getDashboard().getPanels();
        List<IframePanelResponse> responseList = panels.stream()
                .map(panel -> IframePanelResponse.ofNewIframeResponse(
                        dashboard.getDashboard().getUid(),
                        dashboard.getDashboard().getTitle(),
                        panel.getId()))
                .toList();

        return ResponseEntity.ok(responseList).getBody();
    }


    /**
     * 필터 문자열을 파싱하여 Map 형식으로 변환합니다.
     *
     * @param filter "key:value" 형식의 필터 문자열
     * @return 필터 Map
     */
    public Map<String, String> parseFilter(String filter) {
        return Arrays.stream(filter.split(","))
                .map(String::trim)
                .map(entry -> entry.split(":"))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0].trim(), parts -> parts[1].trim()));
    }


    /**
     * 필터 조건에 따라 특정 대시보드에서 표시할 차트만 선택하여 반환합니다.
     *
     * @param dashboardUid 대시보드 UID
     * @param filterMap 필터 Map
     * @return Iframe 응답 패널 목록
     * @throws NotFoundException 패널이 없을 경우
     */
    public List<IframePanelResponse> getFilterCharts(
            String dashboardUid,
            Map<String, String> filterMap) {

        JsonGrafanaDashboardRequest dashboard = grafanaApi.getDashboardInfo(dashboardUid);
        List<Panel> panel = dashboard.getDashboard().getPanels();

        if (panel == null) {
            throw new NotFoundException("panel not found for uid: " + dashboardUid);
        }

        return panel.stream()
                .filter(p -> !"off".equalsIgnoreCase(filterMap.get(p.getTitle())))
                .map(p -> IframePanelResponse.ofNewIframeResponse(dashboardUid, dashboard.getDashboard().getTitle(), p.getId()))
                .toList();
    }

    /**
     * 폴더 제목을 기반으로 해당 폴더 정보를 조회합니다.
     *
     * @param folderTitle 폴더 제목
     * @return 폴더 정보
     * @throws NotFoundException 폴더가 존재하지 않을 경우
     */
    public FolderInfoResponse getFolderByTitle(String folderTitle) {
        return grafanaApi.getAllFolders().stream()
                .filter(folder -> folderTitle.equals(folder.getFolderTitle()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Folder not found: " + folderTitle));
    }

    /**
     * 폴더 제목을 기반으로 폴더 ID를 반환합니다.
     *
     * @param folderTitle 폴더 제목
     * @return 폴더 ID
     */
    public int getFolderIdByTitle(String folderTitle) {
        return getFolderByTitle(folderTitle).getFolderId();
    }

    /**
     * 폴더 제목을 기반으로 폴더 UID를 반환합니다.
     *
     * @param folderTitle 폴더 제목
     * @return 폴더 UID
     */

    public String getFolderUidByTitle(String folderTitle) {
        return getFolderByTitle(folderTitle).getFolderUid();
    }


    /**
     * 대시보드 UID를 기반으로 대시보드 상세 정보를 조회합니다.
     *
     * @param dashboardUid 대시보드 UID
     * @return 대시보드 정보
     * @throws NotFoundException 존재하지 않는 UID일 경우
     */
    public JsonGrafanaDashboardRequest getDashboardInfo(String dashboardUid) {
        JsonGrafanaDashboardRequest dashboard = grafanaApi.getDashboardInfo(dashboardUid);
        if (dashboard == null || dashboard.getDashboard() == null) {
            throw new NotFoundException("Dashboard not found for UID: " + dashboardUid);
        }
        return dashboard;
    }


    /**
     * 주어진 조건에 맞춰 InfluxDB용 Flux 쿼리를 생성합니다.
     *
     * @param measurement 측정 항목
     * @param field 센서 필드 목록
     * @param aggregation 집계 함수 (mean, sum 등)
     * @param time 시간 범위 (예: 1h, 1d)
     * @return Flux 쿼리 문자열
     */
    public String generateFluxQuery(String measurement, List<String> field, String aggregation, String time) {
        String fieldList = field.stream()
                .map(f -> "\"" + f + "\"") // 각 필드를 "field" 형태로 감싸줌
                .collect(Collectors.joining(", ")); // 쉼표로 이어줌

        return String.format("""
                from(bucket: "test")
                  |> range(start: -%s)
                  |> filter(fn: (r) => r["_measurement"] == "%s")
                  |> filter(fn: (r) => contains(value: r["_field"], set: [%s]))
                  |> aggregateWindow(every: 15m, fn: %s, createEmpty: true)
                  |> yield(name: "%s")
                """, time, measurement, fieldList, aggregation, aggregation);
    }

    /**
     * 대시보드 요청을 위한 기본 구조를 생성합니다.
     *
     * @param userId 사용자 ID
     * @param gridPos 차트의 위치 및 크기 정보를 담은 GridPos 객체
     * @param type 패널 타입 (예: "graph", "stat", "table" 등)
     * @param dashboardTitle 대시보드 제목
     * @param panelTitle 생성할 패널 제목
     * @param fluxQuery InfluxDB용 Flux 쿼리
     * @return Grafana 대시보드 요청 객체
     */
    public JsonGrafanaDashboardRequest buildDashboardRequest(String userId, GridPos gridPos, String type, String dashboardTitle, String panelTitle, String fluxQuery) {

        DashboardInfoResponse dashboardInfoResponse = getDashboardInfoRequest(userId, dashboardTitle);

        Datasource datasource = new Datasource(INFLUXDB_UID);

        Target target = new Target(datasource, fluxQuery);
        Panel panel = new Panel(type, panelTitle, gridPos, List.of(target), datasource);

        Dashboard dashboard = new Dashboard(
                dashboardInfoResponse.getDashboardId(),
                dashboardInfoResponse.getDashboardUid(),
                dashboardTitle,
                List.of(panel));

        JsonGrafanaDashboardRequest jsonGrafanaDashboardRequest = new JsonGrafanaDashboardRequest();
        jsonGrafanaDashboardRequest.setDashboard(dashboard);

        return jsonGrafanaDashboardRequest;
    }
}