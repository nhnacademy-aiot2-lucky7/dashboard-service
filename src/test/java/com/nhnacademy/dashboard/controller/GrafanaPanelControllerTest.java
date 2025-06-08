package com.nhnacademy.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.common.advice.CommonAdvice;
import com.nhnacademy.dashboard.dto.dashboard.json.GridPos;
import com.nhnacademy.dashboard.dto.grafana.SensorFieldRequestDto;
import com.nhnacademy.dashboard.dto.panel.*;
import com.nhnacademy.dashboard.exception.BadRequestException;
import com.nhnacademy.dashboard.service.GrafanaPanelService;
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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GrafanaPanelController.class)
@AutoConfigureMockMvc
@Import(CommonAdvice.class)
@AutoConfigureRestDocs
class GrafanaPanelControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    GrafanaPanelService panelService;

    private final List<IframePanelResponse> iframePanelResponses = new ArrayList<>();
    private CreatePanelRequest panelRequest;

    @BeforeEach
    void setUp() {
        iframePanelResponses.add(IframePanelResponse.ofNewIframeResponse(
                "dashboard-uid",
                "dashboard-title",
                1,
                1, 2,null, null, null, null
        ));
        iframePanelResponses.add(IframePanelResponse.ofNewIframeResponse(
                "dashboard-uid2",
                "dashboard-title2",
                2,
                2L
        ));

        panelRequest = CreatePanelRequest.builder()
                .dashboardUid("D-TITLE")
                .panelId(1)
                .panelTitle("P-TITLE")
                .sensorFieldRequestDto(List.of(new SensorFieldRequestDto("battery", 1L, "abc")))
                .gridPos(new GridPos(12, 8))
                .type("time_series")
                .aggregation("mean")
                .time("1d")
                .build();
    }

    @Test
    @DisplayName("패널 조회")
    void getPanel() throws Exception {

        ReadPanelRequest request = new ReadPanelRequest("dashboard-uid");

        Mockito.when(panelService.getPanel(Mockito.any(ReadPanelRequest.class))).thenReturn(iframePanelResponses);

        mockMvc.perform(get("/panels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dashboardUid").value("dashboard-uid"))
                .andExpect(jsonPath("$[0].dashboardTitle").value("dashboard-title"))
                .andExpect(jsonPath("$[0].panelId").value(1))
                .andExpect(jsonPath("$[1].dashboardUid").value("dashboard-uid2"))
                .andExpect(jsonPath("$[1].dashboardTitle").value("dashboard-title2"))
                .andExpect(jsonPath("$[1].panelId").value(2))
                .andDo(document("get-panel"));

        Mockito.verify(panelService, Mockito.times(1)).getPanel(Mockito.any(ReadPanelRequest.class));
    }

    @Test
    @DisplayName("필터링된 패널 조회")
    void getFilterPanel() throws Exception {

        ReadPanelRequest request = new ReadPanelRequest("dashboard-uid");
        List<Integer> offPanelId = new ArrayList<>();
        offPanelId.add(1);

        Mockito.when(panelService.getFilterPanel(Mockito.anyString(), Mockito.anyList())).thenReturn(List.of(iframePanelResponses.get(1)));

        mockMvc.perform(post("/panels/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request))
                        .param("offPanelId", String.valueOf(offPanelId.getFirst())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].panelId").value(2))
                .andExpect(jsonPath("$[0].dashboardUid").value("dashboard-uid2"))
                .andExpect(jsonPath("$[0].dashboardTitle").value("dashboard-title2"))
                .andDo(print())
                .andDo(document("get-filter-panel"));

        Mockito.verify(panelService, Mockito.times(1)).getFilterPanel(Mockito.anyString(), Mockito.anyList());
    }

    @Test
    @DisplayName("패널 생성")
    void createPanel() throws Exception {

        Mockito.doNothing().when(panelService).createPanel(Mockito.anyString(), Mockito.any(CreatePanelRequest.class));


        mockMvc.perform(post("/panels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(panelRequest)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andDo(document("create-panel"));

        Mockito.verify(panelService, Mockito.times(1)).createPanel(Mockito.anyString(), Mockito.any(CreatePanelRequest.class));
    }

    @Test
    @DisplayName("패널 생성: 오타로 실패")
    void createPanel_valid_fail() throws Exception {

        panelRequest = CreatePanelRequest.builder()
                .dashboardUid("D-TITLE")
                .panelId(1)
                .panelTitle("P-TITLE")
                .sensorFieldRequestDto(List.of(new SensorFieldRequestDto("battery", 2L, "abc")))
                .gridPos(new GridPos(12, 8))
                .type("time_ser")
                .aggregation("mean")
                .time("1d")
                .build();

        mockMvc.perform(post("/panels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(panelRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(document("create-panel-valid-fail"));

        Mockito.verify(panelService, Mockito.never()).createPanel(Mockito.anyString(), Mockito.any(CreatePanelRequest.class));
    }

    @Test
    @DisplayName("패널 수정")
    void updatePanel() throws Exception {

        UpdatePanelRequest updatePanelRequest = UpdatePanelRequest.builder()
                .dashboardUid("update-dashboardUid")
                .panelId(1)
                .panelNewTitle("update-panelTitle")
                .sensorFieldRequestDto(List.of(new SensorFieldRequestDto("co2", 3L, "abc")))
                .gridPos(new GridPos(15, 7))
                .type("histogram")
                .aggregation("min")
                .time("3d")
                .build();

        Mockito.doNothing().when(panelService).updatePanel(Mockito.anyString(), Mockito.any(UpdatePanelRequest.class));

        mockMvc.perform(put("/panels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(updatePanelRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("update-panel"));

        Mockito.verify(panelService, Mockito.times(1)).updatePanel(Mockito.anyString(), Mockito.any(UpdatePanelRequest.class));

    }

    @Test
    @DisplayName("우선순위 수정")
    void updatePriority() throws Exception {

        UpdatePanelPriorityRequest updatePanelPriorityRequest = new UpdatePanelPriorityRequest("dashboard-uid", List.of(1, 2, 3));

        Mockito.doNothing().when(panelService).updatePriority(Mockito.anyString(), Mockito.any(UpdatePanelPriorityRequest.class));

        mockMvc.perform(put("/panels/priority")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(updatePanelPriorityRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("update-priority"));

        Mockito.verify(panelService, Mockito.times(1)).updatePriority(Mockito.anyString(), Mockito.any(UpdatePanelPriorityRequest.class));

    }

    @Test
    @DisplayName("패널 삭제")
    void deletePanel() throws Exception {

        DeletePanelRequest deletePanelRequest = new DeletePanelRequest("dashboard-uid", 1);

        Mockito.doNothing().when(panelService).removePanel(Mockito.anyString(), Mockito.any(DeletePanelRequest.class));

        mockMvc.perform(delete("/panels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(deletePanelRequest)))
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andDo(document("delete-panel"));

        Mockito.verify(panelService, Mockito.times(1)).removePanel(Mockito.anyString(), Mockito.any(DeletePanelRequest.class));
    }

    @Test
    @DisplayName("패널 삭제: 요청바디 누락")
    void deletePanel_fail() throws Exception {

        Mockito.doNothing().when(panelService).removePanel(Mockito.anyString(), Mockito.any(DeletePanelRequest.class));

        mockMvc.perform(delete("/panels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Request body 없거나 잘못된 형식입니다")))
                .andDo(document("delete-panel-fail"));

        Mockito.verify(panelService, Mockito.times(0)).removePanel(Mockito.anyString(), Mockito.any(DeletePanelRequest.class));
    }

    @Test
    @DisplayName("패널 삭제: Bad Request")
    void deletePanel_400() throws Exception {

        UpdatePanelPriorityRequest updatePanelPriorityRequest = new UpdatePanelPriorityRequest("dashboard-uid", List.of(1, 2, 3));

        Mockito.doThrow(new BadRequestException("잘못된 요청입니다."))
                .when(panelService)
                .removePanel(Mockito.anyString(), Mockito.any(DeletePanelRequest.class));

        mockMvc.perform(delete("/panels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(updatePanelPriorityRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("CommonException: ")))
                .andDo(document("delete-panel-fail"));
    }

}