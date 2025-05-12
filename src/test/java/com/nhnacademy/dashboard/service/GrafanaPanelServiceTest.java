package com.nhnacademy.dashboard.service;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.dto.dashboard.GrafanaCreateDashboardRequest;
import com.nhnacademy.dashboard.dto.dashboard.InfoDashboardResponse;
import com.nhnacademy.dashboard.dto.dashboard.json.Dashboard;
import com.nhnacademy.dashboard.dto.dashboard.json.Datasource;
import com.nhnacademy.dashboard.dto.dashboard.json.GridPos;
import com.nhnacademy.dashboard.dto.dashboard.json.Panel;
import com.nhnacademy.dashboard.dto.grafana.SensorFieldRequestDto;
import com.nhnacademy.dashboard.dto.panel.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
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
    void setUp() {

        List<Panel> panels = List.of(
                new Panel(
                        1,
                        "p-type",
                        "p-title",
                        new GridPos(9, 12),
                        new ArrayList<>(),
                        new Datasource()
                ),
                new Panel(
                        2,
                        "p-type2",
                        "p-title2",
                        new GridPos(8, 10),
                        new ArrayList<>(),
                        new Datasource()
                ),
                new Panel(
                        3,
                        "p-type3",
                        "p-title3",
                        new GridPos(15, 7, 8),
                        new ArrayList<>(),
                        new Datasource()
                )
        );

        grafanaCreateDashboardRequest = new GrafanaCreateDashboardRequest(
                new Dashboard(
                        1,
                        "D-TITLE",
                        "dashboard-uid",
                        panels,
                        1,
                        1
                ),
                "folder-uid",
                true
        );

        createPanelRequest = new CreatePanelRequest(
                "dashboard-uid",
                1,
                "panel-title",
                List.of(new SensorFieldRequestDto("activity", "gateway-001", "sensor-A1")),
                new GridPos(12, 8),
                "time series",
                "mean",
                "1d");
    }

    @Test
    @DisplayName("패널 생성")
    void createPanel() {


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
                .thenReturn(grafanaCreateDashboardRequest);
        Mockito.when(dashboardService.buildDashboard(grafanaCreateDashboardRequest)).thenReturn(grafanaCreateDashboardRequest.getDashboard());
        Mockito.when(grafanaApi.updateDashboard(Mockito.any(GrafanaCreateDashboardRequest.class)))
                .thenReturn(null);

        panelService.createPanel("1", createPanelRequest);

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
                0L
        );
        Mockito.when(dashboardService.getDashboardInfo(Mockito.anyString())).thenReturn(grafanaCreateDashboardRequest);
        List<IframePanelResponse> iframePanelResponseList = panelService.getPanel(readPanelRequest);

        Assertions.assertNotNull(iframePanelResponseList);
        Assertions.assertAll(
                () -> {
                    Assertions.assertEquals("D-TITLE", iframePanelResponseList.getFirst().getDashboardTitle());
                    Assertions.assertEquals(1, iframePanelResponseList.getFirst().getPanelId());
                    Assertions.assertEquals("dashboard-uid", iframePanelResponseList.getFirst().getDashboardUid());
                }
        );
    }

    @Test
    @DisplayName("off 제외 패널 조회")
    void getFilterPanel() {

        List<Integer> offPanelId = new ArrayList<>();
        offPanelId.add(1);
        Mockito.when(dashboardService.getDashboardInfo(Mockito.anyString())).thenReturn(grafanaCreateDashboardRequest);

        List<IframePanelResponse> result = panelService.getFilterPanel("dashboard-uid", offPanelId);

        Assertions.assertNotNull(result);
        Assertions.assertAll(
                () -> {
                    Assertions.assertEquals(2, result.getFirst().getPanelId());
                    Assertions.assertEquals(3, result.getLast().getPanelId());
                }
        );

    }

    @Test
    @DisplayName("패널 수정")
    void updatePanel() {

        UpdatePanelRequest updatePanelRequest = new UpdatePanelRequest(
                "dashboard-uid",
                1,
                "NEW_PANEL",
                createPanelRequest.getSensorFieldRequestDto(),
                new GridPos(12, 7),
                "histogram",
                "max",
                "3d"
        );

        Mockito.when(folderService.getFolderTitle(Mockito.anyString())).thenReturn("1");
        Mockito.when(folderService.getFolderUidByTitle(Mockito.anyString())).thenReturn("folder-uid");
        Mockito.when(dashboardService.getDashboardInfo(Mockito.anyString())).thenReturn(grafanaCreateDashboardRequest);

        panelService.updatePanel("1", updatePanelRequest);

        Mockito.verify(grafanaApi, Mockito.times(1)).updateDashboard(Mockito.any(GrafanaCreateDashboardRequest.class));

        Assertions.assertEquals("histogram", grafanaCreateDashboardRequest.getDashboard().getPanels().getFirst().getType());
        Assertions.assertEquals("NEW_PANEL", grafanaCreateDashboardRequest.getDashboard().getPanels().getFirst().getTitle());
    }

    @Test
    @DisplayName("우선순위 수정")
    void updatePriority() {

        List<Integer> panelPriority = new ArrayList<>();
        panelPriority.add(2);
        panelPriority.add(3);
        panelPriority.add(1);

        UpdatePanelPriorityRequest updatePanelPriorityRequest = new UpdatePanelPriorityRequest(
                "1",
                panelPriority
        );
        Mockito.when(folderService.getFolderTitle(Mockito.anyString())).thenReturn("folder-title");
        Mockito.when(folderService.getFolderUidByTitle(Mockito.anyString())).thenReturn("folder-uid");
        Mockito.when(dashboardService.getDashboardInfo(Mockito.anyString())).thenReturn(grafanaCreateDashboardRequest);
        Mockito.when(grafanaApi.updateDashboard(Mockito.any(GrafanaCreateDashboardRequest.class))).thenReturn(null);

        panelService.updatePriority("1", updatePanelPriorityRequest);

        Mockito.verify(grafanaApi, Mockito.times(1)).updateDashboard(Mockito.any(GrafanaCreateDashboardRequest.class));

        Assertions.assertAll(
                () -> {
                    Assertions.assertEquals("p-title2", grafanaCreateDashboardRequest.getDashboard().getPanels().getFirst().getTitle());
                    Assertions.assertEquals("p-title3", grafanaCreateDashboardRequest.getDashboard().getPanels().get(1).getTitle());
                    Assertions.assertEquals("p-title", grafanaCreateDashboardRequest.getDashboard().getPanels().get(2).getTitle());
                }
        );
    }

    @Test
    @DisplayName("대시보드 생성 양식 덮어쓰기")
    void overwritten() {

        List<Panel> panels = List.of(
                new Panel(
                        25,
                        "p-type25",
                        "p-title25",
                        new GridPos(9, 12),
                        new ArrayList<>(),
                        new Datasource()
                ));
        GrafanaCreateDashboardRequest overwritten = panelService.overwritten(grafanaCreateDashboardRequest, panels, "folder-uid");
        Assertions.assertAll(
                ()->{
                    Assertions.assertEquals(25,overwritten.getDashboard().getPanels().getFirst().getId());
                    Assertions.assertEquals("p-type25",overwritten.getDashboard().getPanels().getFirst().getType());
                    Assertions.assertEquals("p-title25",overwritten.getDashboard().getPanels().getFirst().getTitle());
                }
        );

    }

    @Test
    @DisplayName("패널 삭제")
    void removePanel() {

        DeletePanelRequest deletePanelRequest = new DeletePanelRequest(
                "dashboard-uid",
                1
        );

        InfoDashboardResponse infoDashboardResponse = new InfoDashboardResponse(
                1,
                "title",
                "uid",
                "f-uid",
                1
                );
        Mockito.when(dashboardService.getDashboardInfo(Mockito.anyString())).thenReturn(grafanaCreateDashboardRequest);
        Mockito.when(dashboardService.getDashboardInfoRequest(Mockito.anyString(), Mockito.anyString())).thenReturn(infoDashboardResponse);
        Mockito.when(dashboardService.buildDashboard(Mockito.any(GrafanaCreateDashboardRequest.class))).thenReturn(grafanaCreateDashboardRequest.getDashboard());
        Mockito.when(grafanaApi.updateDashboard(Mockito.any(GrafanaCreateDashboardRequest.class))).thenReturn(null);

        panelService.removePanel("user123",deletePanelRequest);

        boolean panelExists = grafanaCreateDashboardRequest.getDashboard()
                .getPanels()
                .stream()
                .anyMatch(p -> p.getId().equals(deletePanelRequest.getPanelId()));

        Assertions.assertFalse(panelExists);
    }
}