package com.nhnacademy.dashboard.service;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.dto.front_dto.response.DashboardInfoResponse;
import com.nhnacademy.dashboard.dto.front_dto.update.UpdateDashboardNameRequest;
import com.nhnacademy.dashboard.dto.front_dto.update.UpdatePanelPriorityRequest;
import com.nhnacademy.dashboard.dto.front_dto.update.UpdatePanelRequest;
import com.nhnacademy.dashboard.dto.grafana_dto.JsonGrafanaDashboardRequest;
import com.nhnacademy.dashboard.dto.grafana_dto.dashboard_dto.Dashboard;
import com.nhnacademy.dashboard.dto.grafana_dto.dashboard_dto.GridPos;
import com.nhnacademy.dashboard.dto.grafana_dto.dashboard_dto.Panel;
import com.nhnacademy.dashboard.dto.grafana_dto.dashboard_dto.Target;
import com.nhnacademy.dashboard.exception.BadRequestException;
import com.nhnacademy.dashboard.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateService {

    private final GrafanaApi grafanaApi;
    private final GetService grafanaService;

    /**
     * 대시보드 이름을 수정합니다.
     * - 중복되는 이름이 있는 경우 예외 발생
     *
     * @param userId 사용자 ID
     * @param updateDashboardNameRequest 이름 변경 요청
     * @throws BadRequestException 이미 존재하는 이름일 경우
     */
    public void updateDashboardName(String userId, UpdateDashboardNameRequest updateDashboardNameRequest) {
        JsonGrafanaDashboardRequest existDashboard = grafanaService.getDashboardInfo(updateDashboardNameRequest.getDashboardUid());
        log.info("updateDashboard -> 대시보드 title, uid:{},{}", existDashboard.getDashboard().getTitle(), existDashboard.getDashboard().getUid());

        if(existDashboard.getDashboard().getTitle().equals(updateDashboardNameRequest.getDashboardNewTitle())){
            throw new BadRequestException("이미 존재하는 대시보드 이름입니다.");
        }

        JsonGrafanaDashboardRequest dashboardRequest = new JsonGrafanaDashboardRequest();
        DashboardInfoResponse dashboardInfoResponse = grafanaService.getDashboardInfoRequest(userId, updateDashboardNameRequest.getDashboardNewTitle());
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
     * 기존 대시보드에서 특정 패널의 정보를 수정합니다.
     * - 제목, 차트 타입, 쿼리를 수정하며 기존 대시보드를 overwrite합니다.
     *
     * @param userId 사용자 ID
     * @param request 패널 수정 요청 정보
     */
    public void updateChart(String userId, UpdatePanelRequest request) {

        String folderUid = grafanaService.getFolderUidByTitle(grafanaService.getFolderTitle(userId));
        JsonGrafanaDashboardRequest existDashboard = grafanaService.getDashboardInfo(request.getDashboardUid());
        String fluxQuery = grafanaService.generateFluxQuery(request.getMeasurement(), request.getField(), request.getAggregation(), request.getTime());

        List<Panel> panels = existDashboard.getDashboard().getPanels();
        for (Panel panel : panels) {
            if (panel.getTitle().equals(request.getPanelTitle())) {
                panel.setTitle(request.getPanelNewTitle());
                panel.setType(request.getType());

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
     * 요청된 패널 ID 순서에 따라 각 차트의 우선순위를 재배치합니다.
     *
     * @param userId 사용자 ID
     * @param updatePanelPriorityRequest 패널 우선순위 정보가 담긴 요청 객체
     * @throws NotFoundException 요청된 패널 ID가 존재하지 않을 경우
     */
    public void updatePriority(String userId, UpdatePanelPriorityRequest updatePanelPriorityRequest){
        String folderTitle = grafanaService.getFolderTitle(userId);
        String folderUid = grafanaService.getFolderUidByTitle(folderTitle);
        JsonGrafanaDashboardRequest existDashboard = grafanaService.getDashboardInfo(updatePanelPriorityRequest.getDashboardUid());
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
