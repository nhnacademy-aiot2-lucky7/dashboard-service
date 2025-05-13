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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardMemoryListenerTest {

    @Mock
    private UserApi userApi;

    @Mock
    private GrafanaApi grafanaApi;

    @Mock
    private GrafanaDashboardService grafanaDashboardService;

    @Mock
    private GrafanaFolderService grafanaFolderService;

    @Mock
    private ApplicationReadyEvent event;

    @InjectMocks
    private DashboardMemoryListener listener;

    @Test
    @DisplayName("ApplicationReadyEvent 발생 시 패널 정보 메모리에 저장됨")
    void onApplicationEvent() {

        mockPanelMemoryLoading();

        listener.onApplicationEvent(event);

        Set<Integer> panelIds = DashboardMemory.getPanels("uid-123");
        assertThat(panelIds).containsExactlyInAnyOrder(10, 20);
    }

    @Test
    @DisplayName("loadAllPanelsToMemory() 정상 동작 테스트")
    void loadAllPanelsToMemory() {

        mockPanelMemoryLoading();

        listener.loadAllPanelsToMemory();

        Set<Integer> panelIds = DashboardMemory.getPanels("uid-123");
        assertThat(panelIds).containsExactlyInAnyOrder(10, 20);
    }

    private void mockPanelMemoryLoading() {
        UserDepartmentResponse dept = new UserDepartmentResponse("1","test-dept");
        when(userApi.getDepartments()).thenReturn(List.of(dept));
        when(grafanaFolderService.getFolderIdByTitle("test-dept")).thenReturn(List.of(1));

        InfoDashboardResponse dashboardInfo = new InfoDashboardResponse(
                1, "test-dashboard", "uid-123", "folder-uid", 1
        );
        when(grafanaApi.searchDashboards(List.of(1), "dash-db")).thenReturn(List.of(dashboardInfo));

        Panel panel1 = new Panel();
        panel1.setId(10);
        Panel panel2 = new Panel();
        panel2.setId(20);

        Dashboard dashboard = new Dashboard();
        dashboard.setUid("uid-123");
        dashboard.setPanels(List.of(panel1, panel2));

        GrafanaCreateDashboardRequest dashboardRequest = new GrafanaCreateDashboardRequest();
        dashboardRequest.setDashboard(dashboard);

        when(grafanaDashboardService.getDashboardInfo("uid-123")).thenReturn(dashboardRequest);
    }
}