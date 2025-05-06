package com.nhnacademy.dashboard.service.impl;

import com.nhnacademy.dashboard.api.EventApi;
import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.api.UserApi;
import com.nhnacademy.dashboard.dto.eventdto.Event;
import com.nhnacademy.dashboard.dto.frontdto.response.DashboardInfoResponse;
import com.nhnacademy.dashboard.dto.frontdto.response.FolderInfoResponse;
import com.nhnacademy.dashboard.dto.frontdto.response.IframePanelResponse;
import com.nhnacademy.dashboard.dto.frontdto.create.CreateDashboardRequest;
import com.nhnacademy.dashboard.dto.frontdto.delete.DeleteDashboardRequest;
import com.nhnacademy.dashboard.dto.frontdto.delete.DeletePanelRequest;
import com.nhnacademy.dashboard.dto.frontdto.create.CreatePanelRequest;
import com.nhnacademy.dashboard.dto.frontdto.read.ReadPanelRequest;
import com.nhnacademy.dashboard.dto.frontdto.update.UpdatePanelPriorityRequest;
import com.nhnacademy.dashboard.dto.frontdto.update.UpdatePanelRequest;
import com.nhnacademy.dashboard.dto.frontdto.update.UpdateDashboardNameRequest;
import com.nhnacademy.dashboard.dto.grafanadto.*;
import com.nhnacademy.dashboard.dto.grafanadto.dashboarddto.*;
import com.nhnacademy.dashboard.dto.userdto.UserDepartmentResponse;
import com.nhnacademy.dashboard.dto.userdto.UserInfoResponse;
import com.nhnacademy.dashboard.exception.BadRequestException;
import com.nhnacademy.dashboard.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrafanaServiceImpl {

    private final GrafanaApi grafanaApi;
    public static final String TYPE = "dash-db";
    private static final String INFLUXDB_UID = "o4aKnEJNk";
    private final UserApi userApi;
    private final EventApi eventApi;

    /**
     * 사용자의 부서 정보를 바탕으로 폴더를 조회한 뒤, 해당 폴더에 대시보드를 생성합니다.
     *
     * @param userId 사용자 ID
     * @param createDashboardRequest 대시보드 생성 요청 정보
     */

    public void createDashboard(String userId, CreateDashboardRequest createDashboardRequest) {

        String folderTitle = getFolderTitle(userId);
        log.info("folderTitle:{}", folderTitle);

        int folderId = getFolderIdByTitle(folderTitle);

        grafanaApi.createDashboard(new GrafanaCreateDashboardRequest(new GrafanaCreateDashboardRequest.Dashboard(createDashboardRequest.getDashboardTitle()), folderId));
        Event event = new Event(
                "info",
                createDashboardRequest.getDashboardTitle()+ "대시보드가 생성되었습니다.",
                "createDashboard",
                "CREATE",
                "0",
                LocalDateTime.now());

        eventApi.createEvent(event);
    }

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
     * 주어진 정보에 따라 Grafana에 차트를 생성합니다.
     * - 기존 대시보드가 비어있을 경우 새로 생성합니다.
     * - 차트 제목이 중복될 경우 번호를 붙여 구분합니다.
     *
     * @param userId 사용자 ID
     * @param request 차트 생성 요청 정보
     */
    public void createChart(String userId, CreatePanelRequest request) {

        String folderTitle = getFolderTitle(userId);
        JsonGrafanaDashboardRequest dashboardRequest = new JsonGrafanaDashboardRequest();
        JsonGrafanaDashboardRequest existDashboard = getDashboardInfo(folderTitle);

        // 패널이 존재하지 않는 경우
        if (existDashboard.getDashboard().getPanels().isEmpty()) {
            String fluxQuery = generateFluxQuery(request.getMeasurement(), request.getField(), request.getAggregation(), request.getTime());
            JsonGrafanaDashboardRequest buildDashboardRequest = buildDashboardRequest(userId, request.getGridPos(), request.getType(), folderTitle, request.getTitle(), fluxQuery);

            Dashboard dashboard = getDashboard(buildDashboardRequest);

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
                userId, request.getGridPos(), request.getType(), request.getDashboardTitle(), request.getTitle(), fluxQuery);

        List<Panel> panels = existDashboard.getDashboard().getPanels();
        panels.addAll(buildDashboardRequest.getDashboard().getPanels());
        Dashboard dashboard = getDashboard(buildDashboardRequest);
        dashboard.setPanels(panels);

        dashboardRequest.setDashboard(dashboard);
        dashboardRequest.setFolderUid(getFolderUidByTitle(folderTitle));
        dashboardRequest.setOverwrite(true);

        log.info("CREATE CHART -> request: {}", dashboardRequest);

        grafanaApi.createChart(dashboardRequest);
    }

    /**
     * 중복된 차트 이름에 대해 번호를 붙여 새로운 이름을 생성합니다.
     *
     * @param name 기존 이름
     * @return 중복되지 않는 새로운 이름
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

    private static Dashboard getDashboard(JsonGrafanaDashboardRequest buildDashboardRequest) {
        return new Dashboard(
                buildDashboardRequest.getDashboard().getId(),
                buildDashboardRequest.getDashboard().getUid(),
                buildDashboardRequest.getDashboard().getTitle(),
                buildDashboardRequest.getDashboard().getPanels()
        );
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
     * 기존 대시보드에서 특정 패널의 정보를 수정합니다.
     * - 제목, 차트 타입, 쿼리를 수정하며 기존 대시보드를 overwrite합니다.
     *
     * @param userId 사용자 ID
     * @param request 패널 수정 요청 정보
     */
    public void updateChart(String userId, UpdatePanelRequest request) {

        String folderUid = getFolderUidByTitle(getFolderTitle(userId));
        JsonGrafanaDashboardRequest existDashboard = getDashboardInfo(request.getDashboardUid());
        String fluxQuery = generateFluxQuery(request.getMeasurement(), request.getField(), request.getAggregation(), request.getTime());

        List<Panel> panels = existDashboard.getDashboard().getPanels();
        for (Panel panel : panels) {
            if (panel.getTitle().equals(request.getChartTitle())) {
                panel.setTitle(request.getChartNewTitle());
                panel.setType(request.getGraphType());

                if (panel.getTargets() != null) {
                    for (Target target : panel.getTargets()) {
                        target.setQuery(fluxQuery);
                    }
                }
            }
        }

        JsonGrafanaDashboardRequest dashboardRequest = overwritten(existDashboard, panels, folderUid);

        log.info("UPDATE CHART -> request: {}", fluxQuery);
        grafanaApi.createChart(dashboardRequest);
    }


    /**
     * 대시보드 이름을 수정합니다.
     * - 중복되는 이름이 있는 경우 예외 발생
     *
     * @param userId 사용자 ID
     * @param updateDashboardNameRequest 이름 변경 요청
     * @throws BadRequestException 이미 존재하는 이름일 경우
     */
    public void updateDashboardName(String userId, UpdateDashboardNameRequest updateDashboardNameRequest) {
        JsonGrafanaDashboardRequest existDashboard = getDashboardInfo(updateDashboardNameRequest.getDashboardUid());
        log.info("updateDashboard -> 대시보드 title, uid:{},{}", existDashboard.getDashboard().getTitle(), existDashboard.getDashboard().getUid());

        if(existDashboard.getDashboard().getTitle().equals(updateDashboardNameRequest.getDashboardNewTitle())){
            throw new BadRequestException("이미 존재하는 대시보드 이름입니다.");
        }

        JsonGrafanaDashboardRequest dashboardRequest = new JsonGrafanaDashboardRequest();
        DashboardInfoResponse dashboardInfoResponse = getDashboardInfoRequest(userId, updateDashboardNameRequest.getDashboardNewTitle());
        Dashboard dashboard = new Dashboard(
                dashboardInfoResponse.getDashboardId(),
                dashboardInfoResponse.getDashboardUid(),
                updateDashboardNameRequest.getDashboardNewTitle(),
                existDashboard.getDashboard().getPanels());
        int version = dashboard.getVersion();
        dashboard.setVersion(version+1);

        dashboardRequest.setDashboard(dashboard);
        dashboardRequest.setFolderUid(existDashboard.getFolderUid());
        dashboardRequest.setOverwrite(true);

        log.info("UPDATE CHART Name -> request: {}", dashboardRequest);
        grafanaApi.createChart(dashboardRequest).getBody();
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
    private JsonGrafanaDashboardRequest buildDashboardRequest(String userId, GridPos gridPos, String type, String dashboardTitle, String panelTitle, String fluxQuery) {

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

    /**
     * 사용자의 부서 정보를 기준으로 폴더를 찾아 해당 Grafana 폴더를 삭제합니다.
     *
     * @param userId 사용자 ID
     */
    public void removeFolder(String userId) {
        String folderTitle = getFolderTitle(userId);
        String uid = getFolderUidByTitle(folderTitle);
        grafanaApi.deleteFolder(uid);
    }

    /**
     * 요청된 UID에 해당하는 Grafana 대시보드를 삭제합니다.
     *
     * @param deleteDashboardRequest 삭제할 대시보드 정보를 담은 요청 객체
     */
    public void removeDashboard(DeleteDashboardRequest deleteDashboardRequest) {
        grafanaApi.deleteDashboard(deleteDashboardRequest.getDashboardUid());
    }


    /**
     * 요청된 제목에 해당하는 패널(차트)을 대시보드에서 제거하고 업데이트합니다.
     *
     * @param deletePanelRequest 삭제할 패널 정보를 담은 요청 객체
     */
    public void removeChart(DeletePanelRequest deletePanelRequest) {
        JsonGrafanaDashboardRequest existDashboard = getDashboardInfo(deletePanelRequest.getDashboardUid());
        List<Panel> panels = existDashboard.getDashboard().getPanels();
        panels.removeIf(panel -> panel.getTitle().equals(deletePanelRequest.getChartTitle()));

        Dashboard dashboard = getDashboard(existDashboard);
        dashboard.setPanels(panels);

        existDashboard.setDashboard(dashboard);
        existDashboard.setFolderUid(existDashboard.getFolderUid());
        existDashboard.setOverwrite(true);

        grafanaApi.createChart(existDashboard).getBody();
    }

    /**
     * 요청된 패널 ID 순서에 따라 각 차트의 우선순위를 재배치합니다.
     *
     * @param userId 사용자 ID
     * @param updatePanelPriorityRequest 패널 우선순위 정보가 담긴 요청 객체
     * @throws NotFoundException 요청된 패널 ID가 존재하지 않을 경우
     */
    public void updatePriority(String userId, UpdatePanelPriorityRequest updatePanelPriorityRequest){
        String folderTitle = getFolderTitle(userId);
        String folderUid = getFolderUidByTitle(folderTitle);
        JsonGrafanaDashboardRequest existDashboard = getDashboardInfo(updatePanelPriorityRequest.getDashboardUid());
        List<Panel> panels = existDashboard.getDashboard().getPanels();

        int yPos = 0;
        for(Integer targetPanelId : updatePanelPriorityRequest.getDashboardPriority()){
            Panel panel = panels.stream()
                    .filter(p -> p.getId().equals(targetPanelId))
                    .findFirst()
                    .orElseThrow(()-> new NotFoundException("해당 panelId가 없습니다."));

            GridPos gridPos = panel.getGridPos();
            if (gridPos == null) {
                gridPos = new GridPos();
                panel.setGridPos(gridPos);
            }
            gridPos.setX(0);
            gridPos.setY(yPos);
            gridPos.setW(panel.getGridPos().getW());
            gridPos.setH(panel.getGridPos().getH());

            yPos += panel.getGridPos().getH();
        }

        JsonGrafanaDashboardRequest dashboardRequest = overwritten(existDashboard, panels, folderUid);


        grafanaApi.createChart(dashboardRequest);
    }

    /**
     * 기존 대시보드 정보를 기반으로 새로운 패널 리스트와 폴더 UID를 설정하여
     * overwrite 옵션이 적용된 대시보드 요청 객체를 생성합니다.
     *
     * @param existDashboard 기존 대시보드 정보
     * @param panels 갱신된 패널 리스트
     * @param folderUid 폴더 UID
     * @return 대시보드 요청 객체 (overwrite 포함)
     */
    public JsonGrafanaDashboardRequest overwritten(JsonGrafanaDashboardRequest existDashboard, List<Panel> panels, String folderUid) {
        JsonGrafanaDashboardRequest dashboardRequest = new JsonGrafanaDashboardRequest();
        Dashboard dashboard = new Dashboard(
                existDashboard.getDashboard().getId(),
                existDashboard.getDashboard().getUid(),
                existDashboard.getDashboard().getTitle(),
                panels
        );

        dashboardRequest.setDashboard(dashboard);
        dashboardRequest.setFolderUid(folderUid);
        dashboardRequest.setOverwrite(true);

        return dashboardRequest;
    }
}