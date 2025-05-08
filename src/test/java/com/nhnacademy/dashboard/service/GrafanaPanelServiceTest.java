package com.nhnacademy.dashboard.service;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.dto.dashboard.CreateDashboardRequest;
import com.nhnacademy.dashboard.dto.dashboard.GrafanaCreateDashboardRequest;
import com.nhnacademy.dashboard.dto.dashboard.json.Dashboard;
import com.nhnacademy.dashboard.dto.dashboard.json.Panel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class GrafanaPanelServiceTest {

    @Mock
    GrafanaApi grafanaApi;

    @Mock
    GrafanaFolderService folderService;

    @Mock
    GrafanaDashboardService dashboardService;

    @InjectMocks
    GrafanaPanelService panelService;

    private GrafanaCreateDashboardRequest grafanaCreateDashboardRequest;

    @BeforeEach
    void setUp(){
        grafanaCreateDashboardRequest = new GrafanaCreateDashboardRequest(
                new Dashboard(
                        1,
                        "P-TITLE",
                        "uid",
                        new ArrayList<>(),
                        1,
                        1
                ),
                "folder-uid",
                true
        );
    }

    @Test
    @DisplayName("패널 생성: 기존 패널 없는 경우")
    void createPanel() {

//        CreateDashboardRequest createDashboardRequest = new CreateDashboardRequest();
//        Mockito.when(folderService.getFolderTitle(Mockito.anyString())).thenReturn("folder-title");
//        Mockito.when(dashboardService.getDashboardInfo(Mockito.anyString())).thenReturn(grafanaCreateDashboardRequest);
//
//        panelService.createPanel("1",)
    }

    @Test
    void sameName() {
    }

    @Test
    void getPanel() {
    }

    @Test
    void getFilterPanel() {
    }

    @Test
    void updatePanel() {
    }

    @Test
    void updatePriority() {
    }

    @Test
    void overwritten() {
    }

    @Test
    void removePanel() {
    }
}