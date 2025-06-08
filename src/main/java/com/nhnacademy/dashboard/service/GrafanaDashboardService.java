package com.nhnacademy.dashboard.service;

import com.nhnacademy.common.memory.DashboardMemory;
import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.dto.dashboard.*;
import com.nhnacademy.dashboard.dto.dashboard.json.*;
import com.nhnacademy.dashboard.dto.grafana.SensorFieldRequestDto;
import com.nhnacademy.dashboard.dto.user.UserDepartmentResponse;
import com.nhnacademy.dashboard.exception.BadRequestException;
import com.nhnacademy.dashboard.exception.NotFoundException;
import com.nhnacademy.event.event.EventCreateRequest;
import com.nhnacademy.event.rabbitmq.EventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrafanaDashboardService {

    private final GrafanaApi grafanaApi;
    public static final String TYPE = "dash-db";
    private final GrafanaFolderService grafanaFolderService;
    private final EventProducer eventProducer;
    private static final String DASHBOARD_SOURCE_TYPE = "dashboard";

    /**
     * 사용자 ID를 기반으로 사용자의 부서 폴더에 포함된 대시보드 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 대시보드 목록
     */
    public List<InfoDashboardResponse> getDashboard(String userId) {

        String folderTitle = grafanaFolderService.getFolderTitle(userId).getDepartmentName();
        return grafanaApi.searchDashboards(grafanaFolderService.getFolderIdByTitle(folderTitle), TYPE);
    }

    /**
     * 대시보드 UID를 기반으로 대시보드 상세 정보를 조회합니다.
     *
     * @param dashboardUid 대시보드 UID
     * @return 대시보드 정보
     * @throws NotFoundException 존재하지 않는 UID일 경우
     */
    public GrafanaCreateDashboardRequest getDashboardInfo(String dashboardUid) {
        GrafanaCreateDashboardRequest dashboard = grafanaApi.getDashboardInfo(dashboardUid);
        if (dashboard == null || dashboard.getDashboard() == null) {
            throw new NotFoundException("대시보드의 상세 정보를 조회하지 못했습니다. 해당 UID: " + dashboardUid);
        }
        return dashboard;
    }


    public Dashboard buildDashboard(GrafanaCreateDashboardRequest buildDashboardRequest) {
        return new Dashboard(
                buildDashboardRequest.getDashboard().getId(),
                buildDashboardRequest.getDashboard().getTitle(),
                buildDashboardRequest.getDashboard().getUid(),
                buildDashboardRequest.getDashboard().getPanels()
        );
    }

    /**
     * 주어진 대시보드 제목에 해당하는 대시보드 정보를 반환합니다.
     *
     * @param userId 사용자 ID
     * @param dashboardTitle 대시보드 제목
     * @return 대시보드 정보
     * @throws NotFoundException 해당 제목의 대시보드가 없을 경우
     */
    public InfoDashboardResponse getDashboardInfoRequest(String userId, String dashboardTitle){
        List<InfoDashboardResponse> dashboardInfoResponseList = getDashboard(userId);
        return dashboardInfoResponseList.stream()
                .filter(d -> d.getDashboardTitle().equals(dashboardTitle))
                .findFirst()
                .orElse(null);
    }


    /**
     * 사용자의 부서 정보를 바탕으로 폴더를 조회한 뒤, 해당 폴더에 대시보드를 생성합니다.
     *
     * @param userId                 사용자 ID
     * @param createDashboardRequest 대시보드 생성 요청 정보
     */
    public void createDashboard(String userId, CreateDashboardRequest createDashboardRequest) {

        UserDepartmentResponse departmentResponse = grafanaFolderService.getFolderTitle(userId);
        String departmentId = departmentResponse.getDepartmentId();
        String folderTitle = departmentResponse.getDepartmentName();
        log.info("folderTitle:{}", folderTitle);

        String folderUid = grafanaFolderService.getFolderUidByTitle(folderTitle);

        InfoDashboardResponse dashboardResponse = getDashboardInfoRequest(userId, createDashboardRequest.getDashboardTitle());
        if(dashboardResponse != null){
            throw new BadRequestException("이미 존재하는 대시보드 이름입니다: "+createDashboardRequest.getDashboardTitle());
        }

        GrafanaCreateDashboardRequest request = new GrafanaCreateDashboardRequest(
                new Dashboard(createDashboardRequest.getDashboardTitle(),
                        new ArrayList<>()), folderUid, true);
        grafanaApi.updateDashboard(request);

        EventCreateRequest event = new EventCreateRequest(
          "INFO",
                "대시보드 생성",
                DASHBOARD_SOURCE_TYPE,
                "CREATE",
                departmentId,
                LocalDateTime.now()
        );
        eventProducer.sendEvent(event);
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
        GrafanaCreateDashboardRequest existDashboard = getDashboardInfo(updateDashboardNameRequest.getDashboardUid());
        log.info("updateDashboard -> 대시보드 title, uid:{},{}", existDashboard.getDashboard().getTitle(), existDashboard.getDashboard().getUid());

        if(existDashboard.getDashboard().getTitle().equals(updateDashboardNameRequest.getDashboardNewTitle())){
            throw new BadRequestException("이미 존재하는 대시보드 이름입니다.");
        }

        GrafanaCreateDashboardRequest dashboardRequest = new GrafanaCreateDashboardRequest();
        InfoDashboardResponse dashboardInfoResponse = getDashboardInfoRequest(userId, existDashboard.getDashboard().getTitle());
        Dashboard dashboard = new Dashboard(
                dashboardInfoResponse.getDashboardId(),
                updateDashboardNameRequest.getDashboardNewTitle(),
                dashboardInfoResponse.getDashboardUid(),
                existDashboard.getDashboard().getPanels());
        dashboard.setVersion(existDashboard.getDashboard().getVersion()+1);

        dashboardRequest.setDashboard(dashboard);
        dashboardRequest.setFolderUid(dashboardInfoResponse.getFolderUid());
        dashboardRequest.setOverwrite(true);

        log.info("folderUid : {}", dashboardInfoResponse.getFolderUid());
        grafanaApi.updateDashboard(dashboardRequest);

        String departmentId = grafanaFolderService.getFolderTitle(userId).getDepartmentId();
        EventCreateRequest event = new EventCreateRequest(
                "INFO",
                "대시보드 수정",
                DASHBOARD_SOURCE_TYPE,
                "UPDATE",
                departmentId,
                LocalDateTime.now()
        );
        eventProducer.sendEvent(event);
    }

    /**
     * 요청된 UID에 해당하는 Grafana 대시보드를 삭제합니다.
     *
     * @param deleteDashboardRequest 삭제할 대시보드 정보를 담은 요청 객체
     */
    public void removeDashboard(String userId, DeleteDashboardRequest deleteDashboardRequest) {
        getDashboardInfo(deleteDashboardRequest.getDashboardUid());

        grafanaApi.deleteDashboard(deleteDashboardRequest.getDashboardUid());

        DashboardMemory.clearDashboard(deleteDashboardRequest.getDashboardUid());

        String departmentId = grafanaFolderService.getFolderTitle(userId).getDepartmentId();
        EventCreateRequest event = new EventCreateRequest(
                "INFO",
                "대시보드 삭제",
                DASHBOARD_SOURCE_TYPE,
                "DELETE",
                departmentId,
                LocalDateTime.now()
        );
        eventProducer.sendEvent(event);
    }

    public String getDatasource(){
        List<DataSourceResponse> dataSourceResponse = grafanaApi.getDataSource();
        return dataSourceResponse.getFirst().getUid();
    }

    /**
     * 대시보드 요청을 위한 기본 구조를 생성합니다.
     *
     * @param dashboardBuildRequest 대시보드 생성 요청 정보를 담은 DTO
     * @return Grafana 대시보드 요청 객체
     */
    public GrafanaCreateDashboardRequest buildDashboardRequest(DashboardBuildRequest dashboardBuildRequest) {
        InfoDashboardResponse infoDashboardResponse = getDashboardInfoRequest(
                dashboardBuildRequest.getUserId(),
                dashboardBuildRequest.getDashboardTitle()
        );

        Datasource datasource = new Datasource(getDatasource());

        FieldConfig fieldConfig = buildFieldConfig(dashboardBuildRequest.getMin(), dashboardBuildRequest.getMax());

        Target target = new Target(
                datasource,
                dashboardBuildRequest.getFluxQuery(),
                dashboardBuildRequest.getType()
        );

        Panel panel = Panel.of(
                infoDashboardResponse.getDashboardUid(),
                dashboardBuildRequest.getType(),
                dashboardBuildRequest.getPanelTitle(),
                null,
                dashboardBuildRequest.getGridPos(),
                List.of(target),
                fieldConfig
        );

        Dashboard dashboard = new Dashboard(
                infoDashboardResponse.getDashboardId(),
                dashboardBuildRequest.getDashboardTitle(),
                infoDashboardResponse.getDashboardUid(),
                List.of(panel)
        );

        GrafanaCreateDashboardRequest request = new GrafanaCreateDashboardRequest();
        request.setDashboard(dashboard);

        return request;
    }

    private FieldConfig buildFieldConfig(Double min, Double max) {
        List<FieldConfig.Step> steps = new ArrayList<>();
        steps.add(new FieldConfig.Step("green", null));
        steps.add(new FieldConfig.Step("#EAB839", null));
        steps.add(new FieldConfig.Step("red", null));

        steps.stream()
                .filter(step -> "#EAB839".equals(step.getColor()))
                .findFirst()
                .ifPresent(step -> step.setValue(min));

        steps.stream()
                .filter(step -> "red".equals(step.getColor()))
                .findFirst()
                .ifPresent(step -> step.setValue(max));

        if (min == null && max == null) {
            log.warn("⚠ 임계치가 존재하지 않습니다.");
        } else {
            steps.forEach(step -> log.info("임계치 적용 - 색상: {}, 값: {}", step.getColor(), step.getValue()));
        }

        return new FieldConfig(
                new FieldConfig.Defaults(
                        new FieldConfig.Color(),
                        new FieldConfig.Custom(new FieldConfig.ThresholdsStyle("dashed")),
                        new FieldConfig.Thresholds("absolute", steps)
                )
        );
    }

    public String generateFluxQuery(String bucket, String measurement, SensorFieldRequestDto filters, String aggregation, String time) {
        return String.format("""
            from(bucket: "%s")
              |> range(start: -%s)
              |> filter(fn: (r) => r["_measurement"] == "%s")
              |> filter(fn: (r) => r["_field"] == "%s")
              |> filter(fn: (r) => r["gateway_id"] == "%d")
              |> filter(fn: (r) => r["sensor_id"] == "%s")
              |> aggregateWindow(every: 15m, fn: %s, createEmpty: true)
              |> yield(name: "%s")
            """, bucket, time, measurement, filters.getField(), filters.getGatewayId(), filters.getSensorId(), aggregation, aggregation);
    }
}
