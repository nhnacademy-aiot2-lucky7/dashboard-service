package com.nhnacademy.dashboard.service;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.api.UserApi;
import com.nhnacademy.dashboard.dto.front_dto.create.CreateDashboardRequest;
import com.nhnacademy.dashboard.dto.front_dto.create.CreateFolderRequest;
import com.nhnacademy.dashboard.dto.front_dto.create.CreatePanelRequest;
import com.nhnacademy.dashboard.dto.grafana_dto.GrafanaCreateDashboardRequest;
import com.nhnacademy.dashboard.dto.grafana_dto.JsonGrafanaDashboardRequest;
import com.nhnacademy.dashboard.dto.grafana_dto.dashboard_dto.Dashboard;
import com.nhnacademy.dashboard.dto.grafana_dto.dashboard_dto.Panel;
import com.nhnacademy.dashboard.dto.user_dto.UserDepartmentResponse;
import com.nhnacademy.dashboard.dto.user_dto.UserInfoResponse;
import com.nhnacademy.dashboard.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateService {

    private final GrafanaApi grafanaApi;
    private final GetService grafanaService;
    private final UserApi userApi;


    /**
     * 사용자의 부서 정보를 모두 생성합니다.
     */
    public void createAllFolder() {
        ResponseEntity<List<UserDepartmentResponse>> departmentResponseList = userApi.getDepartment();

        if(!departmentResponseList.getStatusCode().is2xxSuccessful()){
            throw new NotFoundException("부서리스트가 존재하지 않습니다.");
        }

        List<CreateFolderRequest> departmentNameList = Objects.requireNonNull(departmentResponseList.getBody()).stream()
                .map(d -> new CreateFolderRequest(d.getDepartmentName()))
                .toList();

        grafanaApi.createAllFolder(departmentNameList);
    }

    /**
     * 새로운 부서 정보를 개별 생성합니다.
     */
    public void createFolder(String userId) {
        ResponseEntity<UserInfoResponse> userInfoResponse = userApi.getDepartmentId(userId);

        if(!userInfoResponse.getStatusCode().is2xxSuccessful()){
            throw new NotFoundException("유저정보가 존재하지 않습니다.");
        }

        CreateFolderRequest createFolderRequest = new CreateFolderRequest(Objects.requireNonNull(userInfoResponse.getBody()).getUserDepartment());
        grafanaApi.createFolder(createFolderRequest);
    }

    /**
     * 사용자의 부서 정보를 바탕으로 폴더를 조회한 뒤, 해당 폴더에 대시보드를 생성합니다.
     *
     * @param userId                 사용자 ID
     * @param createDashboardRequest 대시보드 생성 요청 정보
     */
    public void createDashboard(String userId, CreateDashboardRequest createDashboardRequest) {

        String folderTitle = grafanaService.getFolderTitle(userId);
        log.info("folderTitle:{}", folderTitle);

        int folderId = grafanaService.getFolderIdByTitle(folderTitle);

        grafanaApi.createDashboard(new GrafanaCreateDashboardRequest(new GrafanaCreateDashboardRequest.Dashboard(createDashboardRequest.getDashboardTitle()), folderId));
    }

    /**
     * 주어진 정보에 따라 Grafana에 차트를 생성합니다.
     * - 기존 대시보드가 비어있을 경우 새로 생성합니다.
     * - 차트 제목이 중복될 경우 번호를 붙여 구분합니다.
     *
     * @param userId  사용자 ID
     * @param request 차트 생성 요청 정보
     */
    public void createChart(String userId, CreatePanelRequest request) {

        String folderTitle = grafanaService.getFolderTitle(userId);
        JsonGrafanaDashboardRequest dashboardRequest = new JsonGrafanaDashboardRequest();
        JsonGrafanaDashboardRequest existDashboard = grafanaService.getDashboardInfo(folderTitle);

        // 패널이 존재하지 않는 경우
        if (existDashboard.getDashboard().getPanels().isEmpty()) {
            String fluxQuery = grafanaService.generateFluxQuery(request.getMeasurement(), request.getField(), request.getAggregation(), request.getTime());
            JsonGrafanaDashboardRequest buildDashboardRequest = grafanaService.buildDashboardRequest(
                    userId,
                    request.getGridPos(),
                    request.getType(),
                    folderTitle,
                    request.getPanelTitle(),
                    fluxQuery);

            Dashboard dashboard = getDashboard(buildDashboardRequest);

            dashboardRequest.setDashboard(dashboard);
            dashboardRequest.setFolderUid(grafanaService.getFolderUidByTitle(folderTitle));
            dashboardRequest.setOverwrite(true);

            log.info("CREATE CHART -> request: {}", dashboardRequest);

            grafanaApi.createChart(dashboardRequest).getBody();
        }

        String fluxQuery = grafanaService.generateFluxQuery(request.getMeasurement(), request.getField(), request.getAggregation(), request.getTime());

        // 이름이 중복된 경우
        if (request.getPanelTitle().equals(existDashboard.getDashboard().getPanels().getFirst().getTitle())) {
            String newTitle = sameName(request.getPanelTitle());
            request.setPanelTitle(newTitle);
        }

        JsonGrafanaDashboardRequest buildDashboardRequest = grafanaService.buildDashboardRequest(
                userId,
                request.getGridPos(),
                request.getType(),
                existDashboard.getDashboard().getTitle(),
                request.getPanelTitle(),
                fluxQuery);

        List<Panel> panels = existDashboard.getDashboard().getPanels();
        panels.addAll(buildDashboardRequest.getDashboard().getPanels());
        Dashboard dashboard = getDashboard(buildDashboardRequest);
        dashboard.setPanels(panels);

        dashboardRequest.setDashboard(dashboard);
        dashboardRequest.setFolderUid(grafanaService.getFolderUidByTitle(folderTitle));
        dashboardRequest.setOverwrite(true);

        log.info("CREATE CHART -> request: {}", dashboardRequest);

        grafanaApi.createChart(dashboardRequest);
    }

    public Dashboard getDashboard(JsonGrafanaDashboardRequest buildDashboardRequest) {
        return new Dashboard(
                buildDashboardRequest.getDashboard().getId(),
                buildDashboardRequest.getDashboard().getUid(),
                buildDashboardRequest.getDashboard().getTitle(),
                buildDashboardRequest.getDashboard().getPanels()
        );
    }

    /**
     * 중복된 차트 이름에 대해 번호를 붙여 새로운 이름을 생성합니다.
     *
     * @param name 기존 이름
     * @return 중복되지 않는 새로운 이름
     */
    public String sameName(String name) {

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

}
