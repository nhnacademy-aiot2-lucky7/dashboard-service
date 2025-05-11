package com.nhnacademy.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.api.UserApi;
import com.nhnacademy.dashboard.dto.dashboard.CreateDashboardRequest;
import com.nhnacademy.dashboard.dto.dashboard.DeleteDashboardRequest;
import com.nhnacademy.dashboard.dto.dashboard.InfoDashboardResponse;
import com.nhnacademy.dashboard.dto.dashboard.UpdateDashboardNameRequest;
import com.nhnacademy.dashboard.dto.folder.FolderInfoResponse;
import com.nhnacademy.dashboard.service.GrafanaDashboardService;
import com.nhnacademy.dashboard.service.GrafanaFolderService;
import com.nhnacademy.dashboard.service.GrafanaPanelService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GrafanaApi grafanaApi;

    @Autowired
    private GrafanaFolderService folderService;

    @Autowired
    private GrafanaDashboardService dashboardService;

    @Autowired
    private GrafanaPanelService panelService;

    @MockitoBean
    private UserApi userApi;

    private List<InfoDashboardResponse> dashboardResponses;
    private CreateDashboardRequest request;

    @BeforeEach
    void setUP() {
        dashboardResponses = new ArrayList<>();
        dashboardResponses.add(new InfoDashboardResponse(1, "D-TITLE", "D-UID", "F-UID", 1));
        dashboardResponses.add(new InfoDashboardResponse(1, "D-TITLE2", "D-UID2", "F-UID2", 1));
        dashboardResponses.add(new InfoDashboardResponse(1, "D-TITLE3", "D-UID3", "F-UID3", 1));

        request = new CreateDashboardRequest("A");
    }

    @Test
    @DisplayName("폴더 조회 - 200 반환")
    void getFolders_200() throws Exception {

        List<FolderInfoResponse> response = grafanaApi.getAllFolders();

        mockMvc.perform(get("/folders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(response.getFirst().getFolderId()))
                .andExpect(jsonPath("$[0].uid").value(response.getFirst().getFolderUid()))
                .andExpect(jsonPath("$[0].title").value(response.getFirst().getFolderTitle()));
    }


    @Test
    @DisplayName("폴더 생성 및 확인")
    void createFolder_actual_check() {
        String departmentName = "TEST Department1";

        folderService.createFolder(departmentName);

        List<FolderInfoResponse> response = folderService.getAllFolders();

        log.info("response:{}", response.getFirst().getFolderTitle());
        boolean found = response.stream()
                .map(FolderInfoResponse::getFolderTitle)
                .anyMatch("TEST Department"::equals);
        Assertions.assertTrue(found);
    }

    @Test
    @DisplayName("대시보드 이름 조회")
    void getDashboardName_actual() throws Exception {

        Mockito.when(dashboardService.getDashboard(Mockito.anyString())).thenReturn(dashboardResponses);

        mockMvc.perform(get("/dashboards/names")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("D-TITLE"))
                .andExpect(jsonPath("$[1]").value("D-TITLE2"))
                .andExpect(jsonPath("$[2]").value("D-TITLE3"))
                .andDo(document("get-dashboard-names-actual"));


        Mockito.verify(dashboardService, Mockito.times(1)).getDashboard("user123");
    }

    @Test
    @DisplayName("모든 대시보드 조회")
    void getAllDashboard_actual() throws Exception {

        Mockito.when(dashboardService.getDashboard(Mockito.anyString())).thenReturn(dashboardResponses);

        mockMvc.perform(get("/dashboards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dashboardTitle").value("D-TITLE"))
                .andExpect(jsonPath("$[1].dashboardUid").value("D-UID2"))
                .andExpect(jsonPath("$[2].folderUid").value("F-UID3"))
                .andDo(document("get-all-dashboards-actual"));


        Mockito.verify(dashboardService, Mockito.times(1)).getDashboard("user123");
    }

    @Test
    @DisplayName("대시보드 생성")
    void createDashboard_actual() throws Exception {

        Mockito.doNothing().when(dashboardService).createDashboard(Mockito.anyString(), Mockito.any(CreateDashboardRequest.class));

        mockMvc.perform(post("/dashboards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andDo(document("create-dashboard-actual"));

        Mockito.verify(dashboardService, Mockito.times(1)).createDashboard(Mockito.anyString(), Mockito.any(CreateDashboardRequest.class));
    }

    @Test
    @DisplayName("대시보드 생성 실패: requestHeader 누락")
    void createDashboard_fail_actual() throws Exception {

        Mockito.doNothing().when(dashboardService).createDashboard(Mockito.anyString(), Mockito.any(CreateDashboardRequest.class));

        mockMvc.perform(post("/dashboards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Required request header 없습니다: ")))
                .andDo(document("create-dashboard-fail-missing-header-actual"));

        Mockito.verify(dashboardService, Mockito.times(0)).createDashboard(Mockito.anyString(), Mockito.any(CreateDashboardRequest.class));
    }

    @Test
    @DisplayName("대시보드 수정")
    void updateDashboard_actual() throws Exception {

        UpdateDashboardNameRequest updateDashboardNameRequest = new UpdateDashboardNameRequest("dashboard-uid", "dashboard-title");

        Mockito.doNothing().when(dashboardService).updateDashboardName(Mockito.anyString(), Mockito.any(UpdateDashboardNameRequest.class));

        mockMvc.perform(put("/dashboards/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(updateDashboardNameRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("update-dashboard-name-actual"));

        Mockito.verify(dashboardService, Mockito.times(1)).updateDashboardName(Mockito.anyString(), Mockito.any(UpdateDashboardNameRequest.class));
    }

    @Test
    @DisplayName("대시보드 삭제")
    void deleteDashboard_actual() throws Exception{

        DeleteDashboardRequest deleteDashboardRequest = new DeleteDashboardRequest("dashboard-uid");

        Mockito.doNothing().when(dashboardService).removeDashboard(Mockito.any(DeleteDashboardRequest.class));

        mockMvc.perform(delete("/dashboards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(deleteDashboardRequest)))
                .andExpect(status().isNoContent())
                .andDo(print())
                .andDo(document("delete-dashboard-actual"));

        Mockito.verify(dashboardService, Mockito.times(1)).removeDashboard(Mockito.any(DeleteDashboardRequest.class));
    }
}
