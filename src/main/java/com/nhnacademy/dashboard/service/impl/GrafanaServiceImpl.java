package com.nhnacademy.dashboard.service.impl;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.api.UserApi;
import com.nhnacademy.dashboard.dto.frontdto.response.DashboardInfoResponse;
import com.nhnacademy.dashboard.dto.frontdto.response.FolderInfoResponse;
import com.nhnacademy.dashboard.dto.frontdto.response.IframePanelResponse;
import com.nhnacademy.dashboard.dto.frontdto.create.CreateDashboardRequest;
import com.nhnacademy.dashboard.dto.frontdto.delete.DeleteDashboardRequest;
import com.nhnacademy.dashboard.dto.frontdto.delete.DeletePanelRequest;
import com.nhnacademy.dashboard.dto.frontdto.create.CreatePanelRequest;
import com.nhnacademy.dashboard.dto.frontdto.read.ReadChartRequest;
import com.nhnacademy.dashboard.dto.frontdto.update.UpdatePanelRequest;
import com.nhnacademy.dashboard.dto.frontdto.update.UpdateDashboardNameRequest;
import com.nhnacademy.dashboard.dto.grafanadto.JsonGrafanaDashboardRequest;
import com.nhnacademy.dashboard.dto.userdto.UserDepartmentResponse;
import com.nhnacademy.dashboard.dto.userdto.UserInfoResponse;
import com.nhnacademy.dashboard.exception.BadRequestException;
import com.nhnacademy.dashboard.exception.NotFoundException;
import com.nhnacademy.dashboard.dto.grafanadto.GrafanaCreateDashboardRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrafanaServiceImpl {

    private final GrafanaApi grafanaApi;
    public static final String TYPE = "dash-db";
    private static final String INFLUXDB_UID = "o4aKnEJNk";
    private static final int GRID_WIDTH = 12;
    private static final int GRID_HEIGHT = 8;
    private final UserApi userApi;


    public void createDashboard(String id, CreateDashboardRequest createDashboardRequest) {

        String folderTitle = getFolderTitle(id);
        log.info("folderTitle:{}", folderTitle);

        int folderId = getFolderIdByTitle(folderTitle);

        grafanaApi.createDashboard(new GrafanaCreateDashboardRequest(new GrafanaCreateDashboardRequest.Dashboard(createDashboardRequest.getDashboardTitle()), folderId));
    }

    public String getFolderTitle(String id){
        UserInfoResponse userInfoResponse = userApi.getDepartmentId(id).getBody();

        if(userInfoResponse == null){
            throw new NotFoundException("user 찾을 수 없습니다: "+id);
        }
        String departmentId = userInfoResponse.getUserDepartment();
        UserDepartmentResponse userDepartmentResponse = userApi.getDepartmentName(departmentId).getBody();

        if(userDepartmentResponse == null){
            throw new NotFoundException("department 찾을 수 없습니다: "+departmentId);
        }
        return userDepartmentResponse.getDepartmentName();
    }

    /**
     * 주어진 필터 조건에 따라 차트를 생성합니다.
     *
     * @param request 차트 생성 요청 정보
     */
    public void createChart(String id, CreatePanelRequest request) {

        String folderTitle = getFolderTitle(id);
        JsonGrafanaDashboardRequest dashboardRequest = new JsonGrafanaDashboardRequest();
        JsonGrafanaDashboardRequest existDashboard = getDashboardInfo(folderTitle);

        // 패널이 존재하지 않는 경우
        if (existDashboard.getDashboard().getPanels().isEmpty()) {
            String fluxQuery = generateFluxQuery(request.getMeasurement(), request.getField(), request.getAggregation(), request.getTime());
            JsonGrafanaDashboardRequest buildDashboardRequest = buildDashboardRequest(request.getType(), folderTitle, request.getTitle(), fluxQuery);

            JsonGrafanaDashboardRequest.Dashboard dashboard = getDashboard(buildDashboardRequest);

            dashboardRequest.setDashboard(dashboard);
            dashboardRequest.setFolderUid(getFolderUidByTitle(folderTitle));
            dashboardRequest.setOverwrite(true);

            log.info("CREATE CHART -> request: {}", dashboardRequest);

            grafanaApi.createChart(dashboardRequest).getBody();
        }

        String fluxQuery = generateFluxQuery(request.getMeasurement(), request.getField(), request.getAggregation(), request.getTime());

        // 이름이 중복된 경우
        if (request.getTitle().equals(existDashboard.getDashboard().getPanels().getFirst().getTitle())) {
            String newTitle = sameName(request.getTitle());
            request.setTitle(newTitle);
        }

        JsonGrafanaDashboardRequest buildDashboardRequest = buildDashboardRequest(
                request.getType(), request.getDashboardTitle(), request.getTitle(), fluxQuery);

        List<JsonGrafanaDashboardRequest.Panel> panels = existDashboard.getDashboard().getPanels();
        panels.addAll(buildDashboardRequest.getDashboard().getPanels());
        JsonGrafanaDashboardRequest.Dashboard dashboard = getDashboard(buildDashboardRequest);
        dashboard.setPanels(panels);

        dashboardRequest.setDashboard(dashboard);
        dashboardRequest.setFolderUid(getFolderUidByTitle(folderTitle));
        dashboardRequest.setOverwrite(true);

        log.info("CREATE CHART -> request: {}", dashboardRequest);

        grafanaApi.createChart(dashboardRequest).getBody();
    }

    /**
     * 중복된 제목 문자열을 입력받아, 숫자를 증가시켜 새로운 제목을 생성합니다.
     * <p>
     * 입력값이 "제목", "제목(1)", "제목(2)" 형식일 경우,
     * 기존 숫자를 1 증가시켜 "제목(2)", "제목(3)"과 같은 새로운 제목을 반환합니다.
     * 만약 숫자가 없는 경우에는 "(1)"을 붙여 반환합니다.
     * </p>
     *
     * @param name 중복된 제목 문자열
     * @return 숫자가 증가된 새로운 제목 문자열
     */
    private String sameName(String name) {

        int index = 1;
        String baseTitle = name;

        int lastOpen = name.lastIndexOf('(');
        int lastClose = name.lastIndexOf(')');

        if (lastOpen != -1 && lastClose == name.length() - 1) {
            String numberPart = name.substring(lastOpen + 1, lastClose);
            try {
                index = Integer.parseInt(numberPart) + 1;
                baseTitle = name.substring(0, lastOpen);
            } catch (NumberFormatException e) {
                // 숫자가 아닌 경우는 무시하고 index = 1, baseTitle = name 유지
                log.info(e.getMessage());
            }
        }

        return String.format("%s(%d)", baseTitle, index);
    }

    /**
     * 전달받은 {@link JsonGrafanaDashboardRequest} 객체로부터 {@link JsonGrafanaDashboardRequest.Dashboard} 객체를 생성합니다.
     * <p>
     * 이 메서드는 요청 객체에 포함된 대시보드 정보를 기반으로 새 {@code Dashboard} 객체를 생성하며,
     * ID, 제목(title), UID, 패널 목록(panels), 스키마 버전(schemaVersion), 버전(version) 등의 정보를 복사합니다.
     *
     * @param buildDashboardRequest 대시보드 정보를 포함하고 있는 {@link JsonGrafanaDashboardRequest} 객체
     * @return 요청으로부터 추출된 정보로 생성된 {@link JsonGrafanaDashboardRequest.Dashboard} 객체
     */
    private static JsonGrafanaDashboardRequest.Dashboard getDashboard(JsonGrafanaDashboardRequest buildDashboardRequest) {
        JsonGrafanaDashboardRequest.Dashboard dashboard = new JsonGrafanaDashboardRequest.Dashboard();
        dashboard.setId(buildDashboardRequest.getDashboard().getId());
        dashboard.setTitle(buildDashboardRequest.getDashboard().getTitle());
        dashboard.setUid(buildDashboardRequest.getDashboard().getUid());
        dashboard.setPanels(buildDashboardRequest.getDashboard().getPanels());
        dashboard.setSchemaVersion(buildDashboardRequest.getDashboard().getSchemaVersion());
        dashboard.setVersion(buildDashboardRequest.getDashboard().getVersion());
        return dashboard;
    }


    /**
     * 모든 폴더 목록을 조회합니다.
     *
     * @return 폴더 리스트
     */
    public List<FolderInfoResponse> getAllFolders() {
        List<FolderInfoResponse> folders = grafanaApi.getAllFolders();

        log.info("전체 폴더: {}", folders.toString());
        return folders;
    }


    public List<DashboardInfoResponse> getDashboard(String id) {

        String folderTitle = getFolderTitle(id);
        List<DashboardInfoResponse> dashboards = grafanaApi.searchDashboards(getFolderIdByTitle(folderTitle), TYPE);
        log.info("getDashboardByTitle -> dashboards: {}", dashboards);
        return dashboards;
    }

    public List<IframePanelResponse> getChart(ReadChartRequest readChartRequest) {

        JsonGrafanaDashboardRequest dashboard = grafanaApi.getDashboardInfo(readChartRequest.getDashboardUid());
        if (dashboard == null) {
            throw new NotFoundException("존재하지 않는 uid : "+readChartRequest.getDashboardUid());
        }

        List<JsonGrafanaDashboardRequest.Panel> panels = dashboard.getDashboard().getPanels();
        List<IframePanelResponse> responseList = panels.stream()
                .map(panel -> IframePanelResponse.ofNewIframeResponse(
                        dashboard.getDashboard().getUid(),
                        dashboard.getDashboard().getTitle(),
                        panel.getId()))
                .toList();

        return ResponseEntity.ok(responseList).getBody();
    }


    /**
     * 필터 문자열을 파싱하여 Map 형태로 변환합니다.
     *
     * @param filter 필터 문자열 (ex. "chart1:on, chart2:off")
     * @return 필터 Map
     */
    public Map<String, String> parseFilter(String filter) {
        return Arrays.stream(filter.split(","))
                .map(String::trim)
                .map(entry -> entry.split(":"))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0].trim(), parts -> parts[1].trim()));
    }


    public List<IframePanelResponse> getFilterCharts(
            String dashboardUid,
            Map<String, String> filterMap) {

        JsonGrafanaDashboardRequest dashboard = grafanaApi.getDashboardInfo(dashboardUid);
        List<JsonGrafanaDashboardRequest.Panel> panel = dashboard.getDashboard().getPanels();

        if (panel == null) {
            throw new NotFoundException("panel not found for uid: " + dashboardUid);
        }

        return panel.stream()
                .filter(p -> !"off".equalsIgnoreCase(filterMap.get(p.getTitle())))
                .map(p -> IframePanelResponse.ofNewIframeResponse(dashboardUid, dashboard.getDashboard().getTitle(), p.getId()))
                .toList();
    }

    /**
     * 주어진 폴더 제목에 해당하는 폴더를 반환합니다.
     *
     * @param folderTitle 폴더 제목
     * @return 폴더 객체
     */
    public FolderInfoResponse getFolderByTitle(String folderTitle) {
        return grafanaApi.getAllFolders().stream()
                .filter(folder -> folderTitle.equals(folder.getFolderTitle()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Folder not found: " + folderTitle));
    }

    /**
     * 폴더 제목에 해당하는 폴더의 ID를 반환합니다.
     *
     * @param folderTitle 폴더 제목
     * @return 폴더 ID
     */
    public int getFolderIdByTitle(String folderTitle) {
        return getFolderByTitle(folderTitle).getFolderId();
    }

    /**
     * 폴더 제목에 해당하는 폴더의 UID를 반환합니다.
     *
     * @param folderTitle 폴더 제목
     * @return 폴더 UID
     */
    public String getFolderUidByTitle(String folderTitle) {
        return getFolderByTitle(folderTitle).getFolderUid();
    }


    /**
     * 주어진 요청 정보를 기반으로 기존 Grafana 대시보드에 차트를 수정합니다.
     * <p>
     * - 기존 대시보드를 조회하여, 새 패널을 panels 리스트에 추가한 뒤 대시보드를 갱신합니다.
     * - overwrite=true 설정을 통해 기존 대시보드를 덮어씁니다.
     *
     * @param request 차트 추가에 필요한 정보를 담은 요청 객체
     *                - folderTitle: 대시보드가 속한 폴더 이름
     *                - dashboardTitle: 패널을 추가할 대시보드 이름
     *                - ChartTitle: 수정할 패널 제목
     *                - title: 새로운 패널 제목
     *                - measurement: 조회할 측정값(Measurement)
     *                - field: 조회할 센서 필드 목록
     *                - type: 생성할 차트 타입 (예: line, bar 등)
     *                - aggregation: 데이터 집계 함수 (예: mean, sum 등)
     *                - time: 조회할 데이터 시간 범위
     */
    public void updateChart(String userId, UpdatePanelRequest request) {

        String folderUid = getFolderUidByTitle(getFolderTitle(userId));
        JsonGrafanaDashboardRequest existDashboard = getDashboardInfo(request.getDashboardUid());
        String fluxQuery = generateFluxQuery(request.getMeasurement(), request.getField(), request.getAggregation(), request.getTime());

        List<JsonGrafanaDashboardRequest.Panel> panels = existDashboard.getDashboard().getPanels();
        for (JsonGrafanaDashboardRequest.Panel panel : panels) {
            if (panel.getTitle().equals(request.getChartTitle())) {
                panel.setTitle(request.getChartNewTitle());
                panel.setType(request.getGraphType());

                if (panel.getTargets() != null) {
                    for (JsonGrafanaDashboardRequest.Target target : panel.getTargets()) {
                        target.setQuery(fluxQuery);
                    }
                }
            }
        }

        JsonGrafanaDashboardRequest dashboardRequest = new JsonGrafanaDashboardRequest();
        JsonGrafanaDashboardRequest.Dashboard dashboard = new JsonGrafanaDashboardRequest.Dashboard();
        dashboard.setId(existDashboard.getDashboard().getId());
        dashboard.setTitle(existDashboard.getDashboard().getTitle());
        dashboard.setPanels(panels);
        dashboard.setSchemaVersion(existDashboard.getDashboard().getSchemaVersion());
        dashboard.setVersion(existDashboard.getDashboard().getVersion());

        dashboardRequest.setDashboard(dashboard);
        dashboardRequest.setFolderUid(folderUid);
        dashboardRequest.setOverwrite(true);

        log.info("UPDATE CHART -> request: {}", dashboardRequest);
        grafanaApi.createChart(dashboardRequest).getBody();
    }


    public void updateDashboardName(UpdateDashboardNameRequest updateDashboardNameRequest) {
        JsonGrafanaDashboardRequest existDashboard = getDashboardInfo(updateDashboardNameRequest.getDashboardUid());
        log.info("updateDashboard -> 대시보드 title, uid:{},{}", existDashboard.getDashboard().getTitle(), existDashboard.getDashboard().getUid());

        if(existDashboard.getDashboard().getTitle().equals(updateDashboardNameRequest.getDashboardNewTitle())){
            throw new BadRequestException("이미 존재하는 대시보드 이름입니다.");
        }

        JsonGrafanaDashboardRequest dashboardRequest = new JsonGrafanaDashboardRequest();
        JsonGrafanaDashboardRequest.Dashboard dashboard = new JsonGrafanaDashboardRequest.Dashboard();
        dashboard.setId(existDashboard.getDashboard().getId());
        dashboard.setTitle(updateDashboardNameRequest.getDashboardNewTitle());
        dashboard.setPanels(existDashboard.getDashboard().getPanels());
        dashboard.setSchemaVersion(existDashboard.getDashboard().getSchemaVersion());
        dashboard.setVersion(existDashboard.getDashboard().getVersion());

        dashboardRequest.setDashboard(dashboard);
        dashboardRequest.setFolderUid(existDashboard.getFolderUid());
        dashboardRequest.setOverwrite(true);

        log.info("UPDATE CHART Name -> request: {}", dashboardRequest);
        grafanaApi.createChart(dashboardRequest).getBody();
    }

    public JsonGrafanaDashboardRequest getDashboardInfo(String dashboardUid) {
        JsonGrafanaDashboardRequest dashboard = grafanaApi.getDashboardInfo(dashboardUid);
        if (dashboard == null || dashboard.getDashboard() == null) {
            throw new NotFoundException("Dashboard not found for UID: " + dashboardUid);
        }
        return dashboard;
    }


    /**
     * Flux 쿼리를 생성합니다.
     *
     * @param measurement 측정 항목
     * @param field       센서 이름
     * @param aggregation 집계 함수
     * @param time        시간 범위
     * @return 생성된 Flux 쿼리
     */
    private String generateFluxQuery(String measurement, List<String> field, String aggregation, String time) {
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
     * 대시보드 요청을 위한 기본 구조를 만듭니다.
     *
     * @param dashboardTitle 대시보드 제목
     * @param panelTitle     패널 제목
     * @param fluxQuery      Flux 쿼리
     * @return 대시보드 요청 정보
     */
    private JsonGrafanaDashboardRequest buildDashboardRequest(String type, String dashboardTitle, String panelTitle, String fluxQuery) {
        JsonGrafanaDashboardRequest.Panel panel = new JsonGrafanaDashboardRequest.Panel();
        panel.setId(null);
        panel.setType(type);
        panel.setTitle(panelTitle);

        JsonGrafanaDashboardRequest.GridPos gridPos = new JsonGrafanaDashboardRequest.GridPos();
        gridPos.setX(0);
        gridPos.setY(0);
        gridPos.setW(GRID_WIDTH);
        gridPos.setH(GRID_HEIGHT);
        panel.setGridPos(gridPos);

        JsonGrafanaDashboardRequest.Target target = new JsonGrafanaDashboardRequest.Target();
        target.setRefId("A");

        JsonGrafanaDashboardRequest.Datasource datasource = new JsonGrafanaDashboardRequest.Datasource();
        datasource.setType("influxdb");
        datasource.setUid(INFLUXDB_UID);

        target.setDatasource(datasource);
        target.setQuery(fluxQuery);
        target.setQueryType("flux");
        target.setResultFormat("time_series");

        panel.setTargets(List.of(target));
        panel.setDatasource(datasource);

        JsonGrafanaDashboardRequest.Dashboard dashboard = new JsonGrafanaDashboardRequest.Dashboard();
        dashboard.setId(0);
        dashboard.setUid(null);
        dashboard.setTitle(dashboardTitle);
        dashboard.setPanels(List.of(panel));
        dashboard.setSchemaVersion(41);
        dashboard.setVersion(0);

        JsonGrafanaDashboardRequest jsonGrafanaDashboardRequest = new JsonGrafanaDashboardRequest();
        jsonGrafanaDashboardRequest.setDashboard(dashboard);

        return jsonGrafanaDashboardRequest;
    }

    public void removeFolder(String id) {
        String folderTitle = getFolderTitle(id);
        String uid = getFolderUidByTitle(folderTitle);
        grafanaApi.deleteFolder(uid);
    }

    public void removeDashboard(DeleteDashboardRequest deleteDashboardRequest) {
        grafanaApi.deleteDashboard(deleteDashboardRequest.getDashboardUid());
    }

    public void removeChart(DeletePanelRequest deletePanelRequest) {
        JsonGrafanaDashboardRequest existDashboard = getDashboardInfo(deletePanelRequest.getDashboardUid());
        List<JsonGrafanaDashboardRequest.Panel> panels = existDashboard.getDashboard().getPanels();
        panels.removeIf(panel -> panel.getTitle().equals(deletePanelRequest.getChartTitle()));

        JsonGrafanaDashboardRequest.Dashboard dashboard = getDashboard(existDashboard);
        dashboard.setPanels(panels);

        existDashboard.setDashboard(dashboard);
        existDashboard.setFolderUid(existDashboard.getFolderUid());
        existDashboard.setOverwrite(true);

        grafanaApi.createChart(existDashboard).getBody();
    }

}