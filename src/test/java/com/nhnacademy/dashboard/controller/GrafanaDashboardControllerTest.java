package com.nhnacademy.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.common.advice.CommonAdvice;
import com.nhnacademy.dashboard.dto.dashboard.CreateDashboardRequest;
import com.nhnacademy.dashboard.dto.dashboard.DeleteDashboardRequest;
import com.nhnacademy.dashboard.dto.dashboard.InfoDashboardResponse;
import com.nhnacademy.dashboard.dto.dashboard.UpdateDashboardNameRequest;
import com.nhnacademy.dashboard.service.GrafanaDashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GrafanaDashboardController.class)
@AutoConfigureMockMvc
@Import(CommonAdvice.class)
@AutoConfigureRestDocs
class GrafanaDashboardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    GrafanaDashboardService dashboardService;

    private List<InfoDashboardResponse> dashboardResponses;
    private CreateDashboardRequest request;

    @BeforeEach
    void setUP() {
        dashboardResponses = new ArrayList<>();
        dashboardResponses.add(new InfoDashboardResponse(1, "D-TITLE", "D-UID", "F-UID", 1));
        dashboardResponses.add(new InfoDashboardResponse(2, "D-TITLE2", "D-UID2", "F-UID", 1));
        dashboardResponses.add(new InfoDashboardResponse(3, "D-TITLE3", "D-UID3", "F-UID", 1));
        dashboardResponses.add(new InfoDashboardResponse(4, "A", "D-UID4", "F-UID", 1));


        request = new CreateDashboardRequest("A");
    }

    @Test
    @DisplayName("대시보드 이름 조회")
    void getDashboardName() throws Exception {

        Mockito.when(dashboardService.getDashboard(Mockito.anyString())).thenReturn(dashboardResponses);

        mockMvc.perform(get("/dashboards/names")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("D-TITLE"))
                .andExpect(jsonPath("$[1]").value("D-TITLE2"))
                .andExpect(jsonPath("$[2]").value("D-TITLE3"))
                .andDo(document("get-dashboard-names"));


        Mockito.verify(dashboardService, Mockito.times(1)).getDashboard("user123");
    }

    @Test
    @DisplayName("모든 대시보드 조회")
    void getAllDashboard() throws Exception {

        Mockito.when(dashboardService.getDashboard(Mockito.anyString())).thenReturn(dashboardResponses);

        mockMvc.perform(get("/dashboards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("D-TITLE"))
                .andExpect(jsonPath("$[1].uid").value("D-UID2"))
                .andExpect(jsonPath("$[2].folderUid").value("F-UID"))
                .andDo(document("get-all-dashboards"));


        Mockito.verify(dashboardService, Mockito.times(1)).getDashboard("user123");
    }

    @Test
    @DisplayName("대시보드 생성")
    void createDashboard() throws Exception {

        Mockito.doNothing().when(dashboardService).createDashboard(Mockito.anyString(), Mockito.any(CreateDashboardRequest.class));

        mockMvc.perform(post("/dashboards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andDo(document("create-dashboard"));

        Mockito.verify(dashboardService, Mockito.times(1)).createDashboard(Mockito.anyString(), Mockito.any(CreateDashboardRequest.class));
    }

    @Test
    @DisplayName("대시보드 생성 실패: requestHeader 누락")
    void createDashboard_fail() throws Exception {

        Mockito.doNothing().when(dashboardService).createDashboard(Mockito.anyString(), Mockito.any(CreateDashboardRequest.class));

        mockMvc.perform(post("/dashboards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Required request header 없습니다: ")))
                .andDo(document("create-dashboard-fail-missing-header"));

        Mockito.verify(dashboardService, Mockito.never()).createDashboard(Mockito.anyString(), Mockito.any(CreateDashboardRequest.class));
    }

    @Test
    @DisplayName("대시보드 수정")
    void updateDashboard() throws Exception {

        UpdateDashboardNameRequest updateDashboardNameRequest = new UpdateDashboardNameRequest("dashboard-uid", "dashboard-title");

        Mockito.doNothing().when(dashboardService).updateDashboardName(Mockito.anyString(),Mockito.any(UpdateDashboardNameRequest.class));

        mockMvc.perform(put("/dashboards/name")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", "user123")
                .content(new ObjectMapper().writeValueAsString(updateDashboardNameRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("update-dashboard-name"));

        Mockito.verify(dashboardService, Mockito.times(1)).updateDashboardName(Mockito.anyString(),Mockito.any(UpdateDashboardNameRequest.class));
    }

    @Test
    @DisplayName("대시보드 수정 -> 서버 내부 에러 발생 (500)")
    void updateDashboard_500() throws Exception {

        UpdateDashboardNameRequest updateDashboardNameRequest =
                new UpdateDashboardNameRequest("dashboard-uid", "dashboard-title");

        Mockito.doThrow(new RuntimeException("Internal Server Error"))
                .when(dashboardService)
                .updateDashboardName(Mockito.anyString(), Mockito.any(UpdateDashboardNameRequest.class));

        mockMvc.perform(put("/dashboards/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(updateDashboardNameRequest)))
                .andExpect(status().isInternalServerError())
                .andDo(print())
                .andDo(document("update-dashboard-name"));
    }

    @Test
    @DisplayName("대시보드 삭제")
    void deleteDashboard() throws Exception{

        DeleteDashboardRequest deleteDashboardRequest = new DeleteDashboardRequest("dashboard-uid");

        Mockito.doNothing().when(dashboardService).removeDashboard(Mockito.any(DeleteDashboardRequest.class));

        mockMvc.perform(delete("/dashboards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(deleteDashboardRequest)))
                .andExpect(status().isNoContent())
                .andDo(print())
                .andDo(document("delete-dashboard"));

        Mockito.verify(dashboardService, Mockito.times(1)).removeDashboard(Mockito.any(DeleteDashboardRequest.class));
    }
}