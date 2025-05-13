package com.nhnacademy.common.listener;

import com.nhnacademy.common.memory.DashboardMemory;
import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.api.UserApi;
import com.nhnacademy.dashboard.dto.dashboard.GrafanaCreateDashboardRequest;
import com.nhnacademy.dashboard.dto.dashboard.InfoDashboardResponse;
import com.nhnacademy.dashboard.dto.dashboard.json.Dashboard;
import com.nhnacademy.dashboard.dto.dashboard.json.Panel;
import com.nhnacademy.dashboard.dto.user.UserDepartmentResponse;
import com.nhnacademy.dashboard.service.GrafanaDashboardService;
import com.nhnacademy.dashboard.service.GrafanaFolderService;
import io.micrometer.common.lang.NonNullApi;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@NonNullApi
@RequiredArgsConstructor
public class DashboardMemoryListener implements ApplicationListener<ApplicationReadyEvent> {

    private final GrafanaDashboardService grafanaDashboardService;
    private final GrafanaApi grafanaApi;
    private final UserApi userApi;
    private final GrafanaFolderService grafanaFolderService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        loadAllPanelsToMemory();
    }

    public void loadAllPanelsToMemory() {
        List<UserDepartmentResponse> departments = userApi.getDepartments();

        for (UserDepartmentResponse department : departments) {
            String folderTitle = department.getDepartmentName();
            List<Integer> folderId = grafanaFolderService.getFolderIdByTitle(folderTitle);

            List<InfoDashboardResponse> dashboards =
                    grafanaApi.searchDashboards(folderId, "dash-db");

            for (InfoDashboardResponse dashboardInfo : dashboards) {
                GrafanaCreateDashboardRequest dashboardRequest =
                        grafanaDashboardService.getDashboardInfo(dashboardInfo.getDashboardUid());

                Dashboard dashboard = dashboardRequest.getDashboard();
                for (Panel panel : dashboard.getPanels()) {
                    if (panel.getId() != null) {
                        DashboardMemory.addPanel(dashboard.getUid(), panel.getId());
                    }
                }
            }
        }
    }
}