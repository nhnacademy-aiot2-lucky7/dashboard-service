package com.nhnacademy.dashboard.service;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.dto.dashboard.CreateDashboardRequest;
import com.nhnacademy.dashboard.dto.dashboard.GrafanaCreateDashboardRequest;
import com.nhnacademy.dashboard.dto.dashboard.json.Dashboard;
import com.nhnacademy.dashboard.dto.dashboard.json.Datasource;
import com.nhnacademy.dashboard.dto.dashboard.json.GridPos;
import com.nhnacademy.dashboard.dto.dashboard.json.Panel;
import com.nhnacademy.dashboard.dto.grafana.SensorFieldRequestDto;
import com.nhnacademy.dashboard.dto.panel.CreatePanelRequest;
import com.nhnacademy.dashboard.dto.panel.IframePanelResponse;
import com.nhnacademy.dashboard.dto.panel.ReadPanelRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private CreatePanelRequest createPanelRequest;

    @BeforeEach
    void setUp(){

        List<Panel> panels = List.of(
                new Panel(
                        "p-type",
                        "p-title",
                        new GridPos(9,12),
                        new ArrayList<>(),
                        new Datasource()
                )
        );

        grafanaCreateDashboardRequest = new GrafanaCreateDashboardRequest(
                new Dashboard(
                        1,
                        "D-TITLE",
                        "uid",
                        panels,
                        1,
                        1
                ),
                "folder-uid",
                true
        );

        createPanelRequest = new CreatePanelRequest(
                "1",
                1,
                "panel-title",
                List.of(new SensorFieldRequestDto("activity", "gateway-001", "sensor-A1")),
                new GridPos(12,8),
                "time series",
                "mean",
                "1d");
    }

    @Test
    @DisplayName("패널 생성: 기존 패널 없는 경우")
    void createPanel() {

        Dashboard builtDashboard = new Dashboard(
                1,
                "P-TITLE",
                "uid",
                new ArrayList<>(List.of(new Panel())),
                1,
                1
        );

        GrafanaCreateDashboardRequest builtRequest = new GrafanaCreateDashboardRequest(
                builtDashboard,
                "folder-uid",
                true
        );

        Mockito.when(folderService.getFolderTitle(Mockito.anyString())).thenReturn("folder-title");
        Mockito.when(dashboardService.getDashboardInfo(Mockito.anyString())).thenReturn(grafanaCreateDashboardRequest);
        Mockito.when(dashboardService.generateFluxQuery(
                Mockito.anyList(),
                Mockito.anyString(),
                Mockito.anyString()
        )).thenReturn("dummy flux query");
        Mockito.when(dashboardService.buildDashboardRequest(
                        Mockito.anyString(),
                        Mockito.any(GridPos.class),
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.anyString()))
                .thenReturn(builtRequest);
        Mockito.when(dashboardService.buildDashboard(builtRequest)).thenReturn(builtDashboard);
        Mockito.when(grafanaApi.updateDashboard(grafanaCreateDashboardRequest)).thenReturn(ResponseEntity.ok(null));

        panelService.createPanel("1",createPanelRequest);

        Mockito.verify(grafanaApi, Mockito.times(1))
                .updateDashboard(Mockito.any(GrafanaCreateDashboardRequest.class));
    }

    @Test
    @DisplayName("중복된 이름에 숫자 붙이기")
    void sameName() {

        String name1 = panelService.sameName("a");
        String name2 = panelService.sameName("a(5)");

        Assertions.assertEquals("a(1)", name1);
        Assertions.assertEquals("a(6)", name2);
    }

    @Test
    @DisplayName("패널 조회")
    void getPanel() {

        ReadPanelRequest readPanelRequest = new ReadPanelRequest(
                "dashboard-uid",
                0l
        );
        Mockito.when(grafanaApi.getDashboardInfo(Mockito.anyString())).thenReturn(grafanaCreateDashboardRequest);
        List<IframePanelResponse> iframePanelResponseList = panelService.getPanel(readPanelRequest);

        Assertions.assertNotNull(iframePanelResponseList);
        Assertions.assertAll(
                ()->{
                    Assertions.assertEquals("D-TITLE",iframePanelResponseList.getFirst().getDashboardTitle());
                    Assertions.assertEquals(1,iframePanelResponseList.getFirst().getPanelId());
                    Assertions.assertEquals("uid",iframePanelResponseList.getFirst().getDashboardUid());
                }
        );
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