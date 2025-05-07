package com.nhnacademy.dashboard.service;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.dto.front_dto.delete.DeleteDashboardRequest;
import com.nhnacademy.dashboard.dto.front_dto.delete.DeletePanelRequest;
import com.nhnacademy.dashboard.dto.grafana_dto.JsonGrafanaDashboardRequest;
import com.nhnacademy.dashboard.dto.grafana_dto.dashboard_dto.Dashboard;
import com.nhnacademy.dashboard.dto.grafana_dto.dashboard_dto.Panel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteService {

    private final GrafanaApi grafanaApi;
    private final GetService grafanaService;

    /**
     * 사용자의 부서 정보를 기준으로 폴더를 찾아 해당 Grafana 폴더를 삭제합니다.
     *
     * @param userId 사용자 ID
     */
    public void removeFolder(String userId) {
        String folderTitle = grafanaService.getFolderTitle(userId);
        String uid = grafanaService.getFolderUidByTitle(folderTitle);
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
        JsonGrafanaDashboardRequest existDashboard = grafanaService.getDashboardInfo(deletePanelRequest.getDashboardUid());
        List<Panel> panels = existDashboard.getDashboard().getPanels();
        panels.removeIf(panel -> panel.getTitle().equals(deletePanelRequest.getChartTitle()));

        Dashboard dashboard = getDashboard(existDashboard);
        dashboard.setPanels(panels);

        existDashboard.setDashboard(dashboard);
        existDashboard.setFolderUid(existDashboard.getFolderUid());
        existDashboard.setOverwrite(true);

        grafanaApi.createChart(existDashboard).getBody();
    }

    public Dashboard getDashboard(JsonGrafanaDashboardRequest buildDashboardRequest) {
        return new Dashboard(
                buildDashboardRequest.getDashboard().getId(),
                buildDashboardRequest.getDashboard().getUid(),
                buildDashboardRequest.getDashboard().getTitle(),
                buildDashboardRequest.getDashboard().getPanels()
        );
    }
}
