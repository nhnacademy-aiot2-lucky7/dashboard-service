package com.nhnacademy.dashboard.service;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.dto.dashboard.*;
import com.nhnacademy.dashboard.dto.dashboard.json.Dashboard;
import com.nhnacademy.dashboard.dto.user.UserDepartmentResponse;
import com.nhnacademy.dashboard.exception.BadRequestException;
import com.nhnacademy.dashboard.exception.NotFoundException;
import com.nhnacademy.event.event.EventCreateRequest;
import com.nhnacademy.event.rabbitmq.EventProducer;
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

import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class GrafanaDashboardServiceTest {

    @Mock
    GrafanaApi grafanaApi;

    @Mock
    GrafanaFolderService folderService;

    @Mock
    private EventProducer eventProducer;

    @InjectMocks
    GrafanaDashboardService dashboardService;

    private InfoDashboardResponse infoDashboardResponse;

    @BeforeEach
    void setUp() {
        infoDashboardResponse = new InfoDashboardResponse(1, "dashboard-title", "dashboard-uid", "folder-uid", 1);
    }

    @Test
    @DisplayName("대시보드 조회")
    void getDashboard() {
        UserDepartmentResponse userDepartmentResponse = new UserDepartmentResponse("1","folder-title");
        Mockito.when(folderService.getFolderTitle(Mockito.anyString())).thenReturn(userDepartmentResponse);
        Mockito.when(grafanaApi.searchDashboards(Mockito.anyString(), Mockito.anyList())).thenReturn(List.of(infoDashboardResponse));

        List<InfoDashboardResponse> infoDashboardResponses = dashboardService.getDashboard("1");

        Assertions.assertNotNull(infoDashboardResponses);
        Assertions.assertAll(
                () -> {
                    Assertions.assertEquals(1, infoDashboardResponses.getFirst().getFolderId());
                    Assertions.assertEquals("dashboard-title", infoDashboardResponses.getFirst().getDashboardTitle());
                    Assertions.assertEquals("dashboard-uid", infoDashboardResponses.getFirst().getDashboardUid());
                    Assertions.assertEquals("folder-uid", infoDashboardResponses.getFirst().getFolderUid());
                    Assertions.assertEquals(1, infoDashboardResponses.getFirst().getDashboardId());
                }
        );
    }

    @Test
    @DisplayName("대시보드 상세 조회")
    void getDashboardInfo() {

        GrafanaCreateDashboardRequest grafanaCreateDashboardRequest = new GrafanaCreateDashboardRequest(
                new Dashboard(1, "dashboard-title", "dashboard-uid", new ArrayList<>(), 1, 1),
                "folder-uid",
                false);

        Mockito.when(grafanaApi.getDashboardInfo("dashboard-uid")).thenReturn(grafanaCreateDashboardRequest);

        GrafanaCreateDashboardRequest result = dashboardService.getDashboardInfo("dashboard-uid");
        Assertions.assertNotNull(result);
        Assertions.assertEquals("folder-uid", result.getFolderUid());
    }

    @Test
    @DisplayName("대시보드 상세 조회: 반환값 null 경우")
    void getDashboardInfo_fail1() {

        Mockito.when(grafanaApi.getDashboardInfo("dashboard-uid")).thenReturn(Mockito.any(GrafanaCreateDashboardRequest.class));

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () -> dashboardService.getDashboardInfo("dashboard-uid"));
        Assertions.assertEquals("대시보드의 상세 정보를 조회하지 못했습니다. 해당 UID: dashboard-uid", exception.getMessage());
    }

    @Test
    @DisplayName("대시보드 상세 조회 : 반환값 내부 대시보드 null 경우")
    void getDashboardInfo_fail2() {

        GrafanaCreateDashboardRequest grafanaCreateDashboardRequest = new GrafanaCreateDashboardRequest(
                null,
                "folder-uid",
                false);

        Mockito.when(grafanaApi.getDashboardInfo("dashboard-uid")).thenReturn(grafanaCreateDashboardRequest);

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () -> dashboardService.getDashboardInfo("dashboard-uid"));
        Assertions.assertEquals("대시보드의 상세 정보를 조회하지 못했습니다. 해당 UID: dashboard-uid", exception.getMessage());
    }

    @Test
    @DisplayName("대시보드 제목에 해당하는 정보 반환")
    void getDashboardInfoRequest() {

        UserDepartmentResponse userDepartmentResponse = new UserDepartmentResponse("1","folder-title");
        Mockito.when(folderService.getFolderTitle(Mockito.anyString())).thenReturn(userDepartmentResponse);
        Mockito.when(grafanaApi.searchDashboards(Mockito.anyString(), Mockito.anyList())).thenReturn(List.of(infoDashboardResponse));

        InfoDashboardResponse infoDashboardResponses = dashboardService.getDashboardInfoRequest("1", "dashboard-title");

        Assertions.assertNotNull(infoDashboardResponses);
        Assertions.assertAll(
                () -> {
                    Assertions.assertEquals(1, infoDashboardResponses.getFolderId());
                    Assertions.assertEquals("dashboard-title", infoDashboardResponses.getDashboardTitle());
                    Assertions.assertEquals("dashboard-uid", infoDashboardResponses.getDashboardUid());
                    Assertions.assertEquals("folder-uid", infoDashboardResponses.getFolderUid());
                    Assertions.assertEquals(1, infoDashboardResponses.getDashboardId());
                }
        );

    }

    @Test
    @DisplayName("대시보드 생성")
    void createDashboard() {

        UserDepartmentResponse userDepartmentResponse = new UserDepartmentResponse("1","folder-title");
        Mockito.when(folderService.getFolderTitle(Mockito.anyString())).thenReturn(userDepartmentResponse);
        Mockito.when(folderService.getFolderUidByTitle(Mockito.anyString())).thenReturn("folder-uid");
        Mockito.when(grafanaApi.updateDashboard(Mockito.any(GrafanaCreateDashboardRequest.class))).thenReturn(null);
        doNothing().when(eventProducer).sendEvent(Mockito.any(EventCreateRequest.class));

        dashboardService.createDashboard("1", new CreateDashboardRequest("dashboard-title"));

        Mockito.verify(grafanaApi, Mockito.times(1)).updateDashboard(Mockito.any(GrafanaCreateDashboardRequest.class));
    }

    @Test
    @DisplayName("대시보드 이름 수정")
    void updateDashboardName() {
        GrafanaCreateDashboardRequest grafanaCreateDashboardRequest = new GrafanaCreateDashboardRequest(
                new Dashboard(1, "dashboard-title", "dashboard-uid", new ArrayList<>(), 1, 1),
                "folder-uid",
                false);

        UserDepartmentResponse userDepartmentResponse = new UserDepartmentResponse("1","folder-title");
        Mockito.when(grafanaApi.getDashboardInfo("dashboard-uid")).thenReturn(grafanaCreateDashboardRequest);
        Mockito.when(folderService.getFolderTitle(Mockito.anyString())).thenReturn(userDepartmentResponse);
        Mockito.when(grafanaApi.searchDashboards(Mockito.anyString(), Mockito.anyList())).thenReturn(List.of(infoDashboardResponse));
        Mockito.when(grafanaApi.updateDashboard(Mockito.any(GrafanaCreateDashboardRequest.class))).thenReturn(null);
        doNothing().when(eventProducer).sendEvent(Mockito.any(EventCreateRequest.class));

        dashboardService.updateDashboardName("user123",new UpdateDashboardNameRequest("dashboard-uid", "NEW TITLE"));

        Mockito.verify(grafanaApi, Mockito.times(1)).updateDashboard(Mockito.any(GrafanaCreateDashboardRequest.class));
    }

    @Test
    @DisplayName("대시보드 이름 중복")
    void updateDashboardName_fail() {
        GrafanaCreateDashboardRequest grafanaCreateDashboardRequest = new GrafanaCreateDashboardRequest(
                new Dashboard(1, "dashboard-title", "dashboard-uid", new ArrayList<>(), 1, 1),
                "folder-uid",
                false);

        UpdateDashboardNameRequest updateDashboardNameRequest = new UpdateDashboardNameRequest("dashboard-uid", "dashboard-title");

        Mockito.when(grafanaApi.getDashboardInfo(Mockito.anyString())).thenReturn(grafanaCreateDashboardRequest);

        BadRequestException exception = Assertions.assertThrows(BadRequestException.class, () ->
                dashboardService.updateDashboardName("1",updateDashboardNameRequest));

        Assertions.assertEquals("이미 존재하는 대시보드 이름입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("대시보드 삭제")
    void removeDashboard() {

        String dashboardUid = "dashboard-uid";
        DeleteDashboardRequest request = new DeleteDashboardRequest(dashboardUid);

        GrafanaCreateDashboardRequest grafanaCreateDashboardRequest = new GrafanaCreateDashboardRequest(
                new Dashboard(1, "dashboard-title", "dashboard-uid", new ArrayList<>(), 1, 1),
                "folder-uid",
                false);

        UserDepartmentResponse userDepartmentResponse = new UserDepartmentResponse("1","folder-title");

        Mockito.when(folderService.getFolderTitle(Mockito.anyString())).thenReturn(userDepartmentResponse);
        Mockito.when(grafanaApi.getDashboardInfo("dashboard-uid")).thenReturn(grafanaCreateDashboardRequest);
        doNothing().when(eventProducer).sendEvent(Mockito.any(EventCreateRequest.class));

        dashboardService.removeDashboard("user123", request);

        Mockito.verify(grafanaApi, Mockito.times(1)).deleteDashboard(dashboardUid);
    }

    @Test
    @DisplayName("대시보드 삭제 실패: uid 없는 경우")
    void removeDashboard_fail() {

        String dashboardUid = "dashboard-uid";
        DeleteDashboardRequest request = new DeleteDashboardRequest(dashboardUid);

        Mockito.when(grafanaApi.getDashboardInfo("dashboard-uid")).thenReturn(Mockito.any(GrafanaCreateDashboardRequest.class));
        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () ->
                dashboardService.removeDashboard("user123", request));
        Assertions.assertEquals("대시보드의 상세 정보를 조회하지 못했습니다. 해당 UID: dashboard-uid", exception.getMessage());
    }
}