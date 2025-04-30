package com.nhnacademy.dashboard.service.impl;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.dto.*;
import com.nhnacademy.dashboard.dto.request.ChartCreateRequest;
import com.nhnacademy.dashboard.dto.request.ChartUpdateRequest;
import com.nhnacademy.dashboard.dto.response.GrafanaDashboardResponse;
import com.nhnacademy.dashboard.exception.NotFoundException;
import com.nhnacademy.dashboard.dto.request.GrafanaCreateDashboardRequest;
import com.nhnacademy.dashboard.dto.response.GrafanaSimpleDashboardResponse;
import com.nhnacademy.dashboard.dto.response.GrafanaFolderResponse;
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

    /**
     * 새로운 대시보드를 생성합니다.
     *
     * @param folderTitle 폴더 이름
     * @param title       생성할 대시보드 제목
     */
    public void createDashboard(String folderTitle, String title) {
        int folderId = getFolderIdByTitle(folderTitle);
        grafanaApi.createDashboard(new GrafanaCreateDashboardRequest(new GrafanaCreateDashboardRequest.Dashboard(title), folderId));
    }


    /**
     * 모든 폴더 목록을 조회합니다.
     *
     * @return 폴더 리스트
     */
    public List<GrafanaFolder> getAllFolders() {
        List<GrafanaFolder> folders = grafanaApi.getAllFolders();

        log.info("전체 폴더: {}", folders.toString());
        return folders;
    }

    /**
     * 폴더 이름을 통해 대시보드 리스트를 조회합니다.
     *
     * @param folderTitle 폴더 이름
     * @return 폴더에 포함된 대시보드 리스트
     */
    public List<GrafanaDashboardInfo> getDashboardByTitle(String folderTitle) {
        List<GrafanaDashboardInfo> dashboards = grafanaApi.searchDashboards(getFolderIdByTitle(folderTitle), TYPE);
        log.info("getDashboardByTitle -> dashboards: {}", dashboards);
        return dashboards;
    }

    /**
     * 특정 폴더와 대시보드 이름으로 차트 목록을 조회합니다.
     *
     * @param folderTitle    폴더 이름
     * @param dashboardTitle 대시보드 이름
     * @return 차트 리스트
     */
    public ResponseEntity<List<GrafanaSimpleDashboardResponse>> getChart(String folderTitle, String dashboardTitle) {

        String uid = findDashboardUid(folderTitle, dashboardTitle);
        GrafanaDashboardPanel panel = grafanaApi.getChart(uid).getBody();

        if (panel == null || panel.getDashboard() == null) {
            throw new NotFoundException("Dashboard panel not found for UID");
        }

        List<GrafanaSimpleDashboardResponse> responseList = panel.getDashboard().getPanels().stream()
                .map(GrafanaSimpleDashboardResponse::from)
                .toList();

        return ResponseEntity.ok(responseList);
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

    /**
     * 필터링 조건에 맞는 차트를 조회합니다.
     *
     * @param folderTitle    폴더 이름
     * @param dashboardTitle 대시보드 이름
     * @param filterMap      필터링 조건
     * @return 필터링된 차트 리스트
     */
    public List<GrafanaFolderResponse> getFilterCharts(
            String folderTitle,
            String dashboardTitle,
            Map<String, String> filterMap) {

        String uid = findDashboardUid(folderTitle, dashboardTitle);
        GrafanaDashboardPanel detail = grafanaApi.getDashboardDetail(uid);

        if (detail == null || detail.getDashboard() == null) {
            throw new NotFoundException("Dashboard details not found for uid: " + uid);
        }

        return detail.getDashboard().getPanels().stream()
                .filter(panel -> !"off".equalsIgnoreCase(filterMap.get(panel.getTitle())))
                .map(panel -> GrafanaFolderResponse.ofGrafanaResponse(panel.getTitle(), uid))
                .toList();
    }


    /**
     * 주어진 필터 조건에 따라 차트를 생성합니다.
     *
     * @param request 차트 생성 요청 정보
     * @return 생성된 대시보드의 응답 정보
     */
    public GrafanaDashboardResponse createChart(String title, ChartCreateRequest request) {

        request.setTitle(title);
        GrafanaDashboard dashboardRequest = new GrafanaDashboard();
        GrafanaDashboard existDashboard = getDashboardInfo(request.getFolderTitle(), request.getDashboardTitle());

        // 패널이 존재하지 않는 경우
        if (existDashboard == null) {
            String fluxQuery = generateFluxQuery(request.getMeasurement(), request.getField(), request.getAggregation(), request.getTime());
            GrafanaDashboard buildDashboardRequest = buildDashboardRequest(request.getType(), request.getDashboardTitle(), request.getTitle(), fluxQuery);

            GrafanaDashboard.Dashboard dashboard = getDashboard(buildDashboardRequest);

            dashboardRequest.setDashboard(dashboard);
            dashboardRequest.setFolderUid(getFolderUidByTitle(request.getFolderTitle()));
            dashboardRequest.setOverwrite(true);

            log.info("CREATE CHART -> request: {}", dashboardRequest);

            return grafanaApi.createChart(dashboardRequest).getBody();
        }

        String fluxQuery = generateFluxQuery(request.getMeasurement(), request.getField(), request.getAggregation(), request.getTime());

        // 이름이 중복된 경우
        if (request.getTitle().equals(existDashboard.getDashboard().getPanels().getFirst().getTitle())) {
            String newTitle = sameName(request.getTitle());
            request.setTitle(newTitle);
        }

        GrafanaDashboard buildDashboardRequest = buildDashboardRequest(
                request.getType(), request.getDashboardTitle(), request.getTitle(), fluxQuery);

        List<GrafanaDashboard.Panel> panels = existDashboard.getDashboard().getPanels();
        panels.addAll(buildDashboardRequest.getDashboard().getPanels());
        GrafanaDashboard.Dashboard dashboard = getDashboard(buildDashboardRequest);
        dashboard.setPanels(panels);

        dashboardRequest.setDashboard(dashboard);
        dashboardRequest.setFolderUid(getFolderUidByTitle(request.getFolderTitle()));
        dashboardRequest.setOverwrite(true);

        log.info("CREATE CHART -> request: {}", dashboardRequest);

        return grafanaApi.createChart(dashboardRequest).getBody();
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
     * 전달받은 {@link GrafanaDashboard} 객체로부터 {@link GrafanaDashboard.Dashboard} 객체를 생성합니다.
     * <p>
     * 이 메서드는 요청 객체에 포함된 대시보드 정보를 기반으로 새 {@code Dashboard} 객체를 생성하며,
     * ID, 제목(title), UID, 패널 목록(panels), 스키마 버전(schemaVersion), 버전(version) 등의 정보를 복사합니다.
     *
     * @param buildDashboardRequest 대시보드 정보를 포함하고 있는 {@link GrafanaDashboard} 객체
     * @return 요청으로부터 추출된 정보로 생성된 {@link GrafanaDashboard.Dashboard} 객체
     */
    private static GrafanaDashboard.Dashboard getDashboard(GrafanaDashboard buildDashboardRequest) {
        GrafanaDashboard.Dashboard dashboard = new GrafanaDashboard.Dashboard();
        dashboard.setId(buildDashboardRequest.getDashboard().getId());
        dashboard.setTitle(buildDashboardRequest.getDashboard().getTitle());
        dashboard.setUid(buildDashboardRequest.getDashboard().getUid());
        dashboard.setPanels(buildDashboardRequest.getDashboard().getPanels());
        dashboard.setSchemaVersion(buildDashboardRequest.getDashboard().getSchemaVersion());
        dashboard.setVersion(buildDashboardRequest.getDashboard().getVersion());
        return dashboard;
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
     * @return 갱신된 대시보드에 대한 응답 객체
     */
    public GrafanaDashboardResponse updateChart(ChartUpdateRequest request) {

        GrafanaDashboard existDashboard = getDashboardInfo(request.getFolderTitle(), request.getDashboardTitle());
        String fluxQuery = generateFluxQuery(request.getMeasurement(), request.getField(), request.getAggregation(), request.getTime());

        List<GrafanaDashboard.Panel> panels = existDashboard.getDashboard().getPanels();
        for (GrafanaDashboard.Panel panel : panels) {
            if (panel.getTitle().equals(request.getChartTitle())) {
                panel.setTitle(request.getTitle());
                panel.setType(request.getType());

                if (panel.getTargets() != null) {
                    for (GrafanaDashboard.Target target : panel.getTargets()) {
                        target.setQuery(fluxQuery);
                    }
                }
            }
        }

        GrafanaDashboard dashboardRequest = new GrafanaDashboard();
        GrafanaDashboard.Dashboard dashboard = new GrafanaDashboard.Dashboard();
        dashboard.setId(existDashboard.getDashboard().getId());
        dashboard.setTitle(existDashboard.getDashboard().getTitle());
        dashboard.setPanels(panels);
        dashboard.setSchemaVersion(existDashboard.getDashboard().getSchemaVersion());
        dashboard.setVersion(existDashboard.getDashboard().getVersion());

        dashboardRequest.setDashboard(dashboard);
        dashboardRequest.setFolderUid(getFolderUidByTitle(request.getFolderTitle()));
        dashboardRequest.setOverwrite(true);

        log.info("UPDATE CHART -> request: {}", dashboardRequest);
        return grafanaApi.createChart(dashboardRequest).getBody();
    }

    /**
     * 대시보드 제목을 수정합니다.
     *
     * @param folderTitle    폴더 이름
     * @param dashboardTitle 수정할 대시보드 제목
     * @param updateTitle    수정된 대시보드 제목
     * @return 수정된 대시보드 응답
     */
    public GrafanaDashboardResponse updateDashboardName(String folderTitle, String dashboardTitle, String updateTitle) {
        GrafanaDashboard existDashboard = getDashboardInfo(folderTitle, dashboardTitle);
        log.info("updateDashboard -> 대시보드 title, uid:{},{}", existDashboard.getDashboard().getTitle(), existDashboard.getDashboard().getUid());

        GrafanaDashboard dashboardRequest = new GrafanaDashboard();
        GrafanaDashboard.Dashboard dashboard = new GrafanaDashboard.Dashboard();
        dashboard.setId(existDashboard.getDashboard().getId());
        dashboard.setTitle(updateTitle);
        dashboard.setPanels(existDashboard.getDashboard().getPanels());
        dashboard.setSchemaVersion(existDashboard.getDashboard().getSchemaVersion());
        dashboard.setVersion(existDashboard.getDashboard().getVersion());

        dashboardRequest.setDashboard(dashboard);
        dashboardRequest.setFolderUid(getFolderUidByTitle(folderTitle));
        dashboardRequest.setOverwrite(true);

        log.info("UPDATE CHART Name -> request: {}", dashboardRequest);
        return grafanaApi.createChart(dashboardRequest).getBody();
    }

    /**
     * 주어진 폴더와 대시보드 이름에 해당하는 대시보드 정보를 반환합니다.
     *
     * @param folderTitle    폴더 이름
     * @param dashboardTitle 대시보드 이름
     * @return 해당 대시보드 정보
     */
    public GrafanaDashboard getDashboardInfo(String folderTitle, String dashboardTitle) {
        String uid = findDashboardUid(folderTitle, dashboardTitle);
        GrafanaDashboard dashboard = grafanaApi.getDashboardInfo(uid);
        if (dashboard == null || dashboard.getDashboard() == null) {
            throw new NotFoundException("Dashboard not found for UID: " + uid);
        }
        return dashboard;
    }

    /**
     * 주어진 폴더와 대시보드 이름에 해당하는 대시보드 UID를 찾습니다.
     *
     * @param folderTitle    폴더 이름
     * @param dashboardTitle 대시보드 이름
     * @return 대시보드 UID
     */
    public String findDashboardUid(String folderTitle, String dashboardTitle) {
        return getDashboardByTitle(folderTitle).stream()
                .filter(d -> dashboardTitle.equals(d.getTitle()))
                .findFirst()
                .map(GrafanaDashboardInfo::getUid)
                .orElseThrow(() -> new NotFoundException("Dashboard not found: " + dashboardTitle));
    }

    /**
     * 주어진 폴더 제목에 해당하는 폴더를 반환합니다.
     *
     * @param folderTitle 폴더 제목
     * @return 폴더 객체
     */
    public GrafanaFolder getFolderByTitle(String folderTitle) {
        return grafanaApi.getAllFolders().stream()
                .filter(folder -> folderTitle.equals(folder.getTitle()))
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
        return getFolderByTitle(folderTitle).getId();
    }

    /**
     * 폴더 제목에 해당하는 폴더의 UID를 반환합니다.
     *
     * @param folderTitle 폴더 제목
     * @return 폴더 UID
     */
    public String getFolderUidByTitle(String folderTitle) {
        return getFolderByTitle(folderTitle).getUid();
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
    private GrafanaDashboard buildDashboardRequest(String type, String dashboardTitle, String panelTitle, String fluxQuery) {
        GrafanaDashboard.Panel panel = new GrafanaDashboard.Panel();
        panel.setId(null);
        panel.setType(type);
        panel.setTitle(panelTitle);

        GrafanaDashboard.GridPos gridPos = new GrafanaDashboard.GridPos();
        gridPos.setX(0);
        gridPos.setY(0);
        gridPos.setW(GRID_WIDTH);
        gridPos.setH(GRID_HEIGHT);
        panel.setGridPos(gridPos);

        GrafanaDashboard.Target target = new GrafanaDashboard.Target();
        target.setRefId("A");

        GrafanaDashboard.Datasource datasource = new GrafanaDashboard.Datasource();
        datasource.setType("influxdb");
        datasource.setUid(INFLUXDB_UID);

        target.setDatasource(datasource);
        target.setQuery(fluxQuery);
        target.setQueryType("flux");
        target.setResultFormat("time_series");

        panel.setTargets(List.of(target));
        panel.setDatasource(datasource);

        GrafanaDashboard.Dashboard dashboard = new GrafanaDashboard.Dashboard();
        dashboard.setId(0);
        dashboard.setUid(null);
        dashboard.setTitle(dashboardTitle);
        dashboard.setPanels(List.of(panel));
        dashboard.setSchemaVersion(41);
        dashboard.setVersion(0);

        GrafanaDashboard grafanaDashboard = new GrafanaDashboard();
        grafanaDashboard.setDashboard(dashboard);

        return grafanaDashboard;
    }

    /**
     * 지정한 폴더 제목을 기반으로 폴더를 삭제합니다.
     *
     * @param folderTitle 삭제할 폴더의 제목
     */
    public void removeFolder(String folderTitle) {
        String uid = getFolderUidByTitle(folderTitle);
        grafanaApi.deleteFolder(uid);
    }

    /**
     * 지정한 폴더와 대시보드 제목을 기반으로 해당 대시보드를 삭제합니다.
     *
     * @param folderTitle    대시보드가 속한 폴더의 제목
     * @param dashboardTitle 삭제할 대시보드의 제목
     */
    public void removeDashboard(String folderTitle, String dashboardTitle) {
        grafanaApi.deleteDashboard(findDashboardUid(folderTitle, dashboardTitle));
    }

    /**
     * 지정한 폴더와 대시보드 내에서 특정 차트를 찾아 삭제합니다.
     * <p>
     * 삭제 후 대시보드를 덮어쓰기(overwrite) 방식으로 갱신하여 차트를 제거합니다.
     * </p>
     *
     * @param folderTitle    차트가 속한 폴더의 제목
     * @param dashboardTitle 차트가 속한 대시보드의 제목
     * @param chartTitle     삭제할 차트의 제목
     * @return 차트 삭제 후 갱신된 대시보드 응답 객체
     */
    public GrafanaDashboardResponse removeChart(String folderTitle, String dashboardTitle, String chartTitle) {
        GrafanaDashboard existDashboard = getDashboardInfo(folderTitle, dashboardTitle);
        List<GrafanaDashboard.Panel> panels = existDashboard.getDashboard().getPanels();
        panels.removeIf(panel -> panel.getTitle().equals(chartTitle));

        GrafanaDashboard.Dashboard dashboard = getDashboard(existDashboard);
        dashboard.setPanels(panels);

        existDashboard.setDashboard(dashboard);
        existDashboard.setFolderUid(getFolderUidByTitle(folderTitle));
        existDashboard.setOverwrite(true);

        return grafanaApi.createChart(existDashboard).getBody();
    }
}