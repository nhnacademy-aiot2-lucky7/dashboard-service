package com.nhnacademy.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.common.advice.CommonAdvice;
import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.api.UserApi;
import com.nhnacademy.dashboard.dto.dashboard.CreateDashboardRequest;
import com.nhnacademy.dashboard.dto.dashboard.DeleteDashboardRequest;
import com.nhnacademy.dashboard.dto.dashboard.InfoDashboardResponse;
import com.nhnacademy.dashboard.dto.dashboard.UpdateDashboardNameRequest;
import com.nhnacademy.dashboard.dto.dashboard.json.GridPos;
import com.nhnacademy.dashboard.dto.folder.FolderInfoResponse;
import com.nhnacademy.dashboard.dto.grafana.SensorFieldRequestDto;
import com.nhnacademy.dashboard.dto.panel.*;
import com.nhnacademy.dashboard.dto.user.UserDepartmentResponse;
import com.nhnacademy.dashboard.dto.user.UserInfoResponse;
import com.nhnacademy.dashboard.service.GrafanaDashboardService;
import com.nhnacademy.dashboard.service.GrafanaFolderService;
import com.nhnacademy.dashboard.service.GrafanaPanelService;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(CommonAdvice.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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

    private UserInfoResponse userInfoResponse;

    @BeforeEach
    void setUp(){
        userInfoResponse = new UserInfoResponse(
                "user",
                "1",
                "kim",
                "kim@email.com",
                "010-1111-2222",
                new UserDepartmentResponse("1", "TEST Department")
        );
    }

    @AfterAll
    void tearDown() {
        grafanaApi.getAllFolders().stream()
                .filter(f -> f.getFolderTitle().equals("TEST Department"))
                .forEach(f -> {
                    try {
                        grafanaApi.deleteFolder(f.getFolderUid());
                        log.info("uid:{}", f.getFolderUid());
                    } catch (Exception e) {
                        // 로그만 남기고 무시
                        log.warn("폴더 삭제 실패 (무시됨): {}", e.getMessage());
                    }
                });
    }

    @Test
    @Order(1)
    @DisplayName("폴더 생성 및 조회")
    void createFolder_actual_check() {
        String departmentName = "TEST Department";

        folderService.createFolder(departmentName);

        List<FolderInfoResponse> response = folderService.getAllFolders();

        log.info("response:{}", response.getFirst().getFolderTitle());
        boolean found = response.stream()
                .map(FolderInfoResponse::getFolderTitle)
                .anyMatch("TEST Department"::equals);
        Assertions.assertTrue(found);
    }

    @Test
    @Order(2)
    @DisplayName("대시보드 생성 및 조회")
    void createDashboard_actual() throws Exception {

        CreateDashboardRequest request = new CreateDashboardRequest("A");
        CreateDashboardRequest request2 = new CreateDashboardRequest("B");
        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);

        mockMvc.perform(post("/dashboards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andDo(document("create-dashboard-actual"));

        List<InfoDashboardResponse> response = dashboardService.getDashboard("user123");

        boolean found = response.stream()
                .map(InfoDashboardResponse::getDashboardTitle)
                .anyMatch("A"::equals);
        Assertions.assertTrue(found);

        dashboardService.createDashboard("user123",request2);
    }

    @Test
    @Order(3)
    @DisplayName("대시보드 생성 실패: 중복된 이름")
    void createDashboard_duplicated_fail_actual() throws Exception {
        CreateDashboardRequest request = new CreateDashboardRequest("A");

        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);
        mockMvc.perform(post("/dashboards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(Matchers.containsString("이미 존재하는 대시보드 이름입니다: ")))
                .andDo(document("create-dashboard-duplicated-fail-actual"));
    }

    @Test
    @Order(4)
    @DisplayName("대시보드 생성 실패: requestHeader 누락")
    void createDashboard_request_header_null_fail_actual() throws Exception {

        CreateDashboardRequest request = new CreateDashboardRequest("A");

        mockMvc.perform(post("/dashboards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(Matchers.containsString("Required request header 없습니다: ")))
                .andDo(document("create-dashboard-request-header-null-fail-actual"));
    }

    @Test
    @Order(5)
    @DisplayName("대시보드 이름 조회")
    void getDashboardName_actual() throws Exception {

        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);

        mockMvc.perform(get("/dashboards/names")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("A"))
                .andDo(document("get-dashboard-names-actual"));
    }

    @Test
    @Order(6)
    @DisplayName("대시보드 수정")
    void updateDashboard_actual() throws Exception {

        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);

        InfoDashboardResponse infoDashboardResponse = dashboardService.getDashboardInfoRequest("user123", "B");
        UpdateDashboardNameRequest updateDashboardNameRequest = new UpdateDashboardNameRequest(
                infoDashboardResponse.getDashboardUid(),
                "B-NEW123");

        mockMvc.perform(put("/dashboards/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(updateDashboardNameRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("update-dashboard-name-actual"));
    }

    @Test
    @Order(7)
    @DisplayName("패널 생성")
    void createPanel_actual() throws Exception {

        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);

        InfoDashboardResponse infoDashboardResponse = dashboardService.getDashboardInfoRequest("user123", "A");
        CreatePanelRequest panelRequest = new CreatePanelRequest(
                infoDashboardResponse.getDashboardUid(),
                1,
                "P-TITLE1",
                List.of(new SensorFieldRequestDto("battery", "12345", "abc")),
                new GridPos(12, 8),
                "time_series",
                "mean",
                "1d");
        CreatePanelRequest panelRequest2 = new CreatePanelRequest(
                infoDashboardResponse.getDashboardUid(),
                2,
                "P-TITLE2",
                List.of(new SensorFieldRequestDto("co", "12345", null)),
                new GridPos(12, 8),
                "time_series",
                "min",
                "5d");

        CreatePanelRequest panelRequest3 = new CreatePanelRequest(
                infoDashboardResponse.getDashboardUid(),
                3,
                "P-TITLE3",
                List.of(new SensorFieldRequestDto("humidity", null, "abc")),
                new GridPos(12, 8),
                "histogram",
                "max",
                "2d");

        mockMvc.perform(post("/panels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(panelRequest)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andDo(document("create-panel-actual"));

        panelService.createPanel("user123",panelRequest2);
        panelService.createPanel("user123",panelRequest3);
    }

    @Test
    @Order(7)
    @DisplayName("필터링된 패널 조회")
    void getFilterPanel_actual() throws Exception {

        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);

        InfoDashboardResponse infoDashboardResponse = dashboardService.getDashboardInfoRequest("user123", "A");

        ReadPanelRequest request = new ReadPanelRequest(infoDashboardResponse.getDashboardUid(), 1L);

        mockMvc.perform(get("/panels/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request))
                        .param("offPanelId","1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].panelId").value(2))
                .andExpect(jsonPath("$[1].panelId").value(3))
                .andDo(print())
                .andDo(document("get-filter-panel-actual"));
    }

    @Test
    @Order(8)
    @DisplayName("패널 수정")
    void updatePanel_actual() throws Exception {

        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);

        InfoDashboardResponse infoDashboardResponse = dashboardService.getDashboardInfoRequest("user123", "A");
        UpdatePanelRequest updatePanelRequest = new UpdatePanelRequest(
                infoDashboardResponse.getDashboardUid(),
                1,
                "update-panelTitle",
                List.of(new SensorFieldRequestDto("co2", "12345", "abc")),
                new GridPos(15, 7),
                "histogram",
                "min",
                "3d"
        );

        mockMvc.perform(put("/panels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(updatePanelRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("update-panel-actual"));
    }

    @Test
    @Order(8)
    @DisplayName("우선순위 수정")
    void updatePriority_actual() throws Exception {

        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);

        InfoDashboardResponse infoDashboardResponse = dashboardService.getDashboardInfoRequest("user123", "A");
        UpdatePanelPriorityRequest updatePanelPriorityRequest = new UpdatePanelPriorityRequest(
                infoDashboardResponse.getDashboardUid(),
                List.of(3,2,1));

        mockMvc.perform(put("/panels/priority")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(updatePanelPriorityRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("update-priority-actual"));

    }

    @Test
    @Order(9)
    @DisplayName("패널 조회")
    void getPanel_actual() throws Exception {

        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);

        InfoDashboardResponse infoDashboardResponse = dashboardService.getDashboardInfoRequest("user123", "A");
        ReadPanelRequest request = new ReadPanelRequest(infoDashboardResponse.getDashboardUid(), 1L);

        mockMvc.perform(get("/panels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].panelId").value(3))
                .andExpect(jsonPath("$[1].panelId").value(2))
                .andExpect(jsonPath("$[2].panelId").value(1))
                .andDo(document("get-panel-actual"));
    }

    @Test
    @Order(10)
    @DisplayName("패널 삭제")
    void deletePanel_actual() throws Exception {

        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);

        InfoDashboardResponse infoDashboardResponse = dashboardService.getDashboardInfoRequest("user123", "A");
        DeletePanelRequest deletePanelRequest = new DeletePanelRequest(infoDashboardResponse.getDashboardUid(), 2);
        DeletePanelRequest deletePanelRequest1 = new DeletePanelRequest(infoDashboardResponse.getDashboardUid(), 1);

        panelService.removePanel("user123",deletePanelRequest1);

        mockMvc.perform(delete("/panels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(deletePanelRequest)))
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andDo(document("delete-panel-actual"));
    }

    @Test
    @Order(11)
    @DisplayName("패널 삭제: 잘못된 요청")
    void deletePanel_400_actual() throws Exception {

        mockMvc.perform(delete("/panels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(""))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(document("delete-panel-400-actual"));
    }


    @Test
    @Order(12)
    @DisplayName("대시보드 삭제")
    void deleteDashboard_actual() throws Exception {

        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);

        InfoDashboardResponse infoDashboardResponse = dashboardService.getDashboardInfoRequest("user123", "B-NEW123");
        DeleteDashboardRequest deleteDashboardRequest = new DeleteDashboardRequest(infoDashboardResponse.getDashboardUid());

        mockMvc.perform(delete("/dashboards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(deleteDashboardRequest)))
                .andExpect(status().isNoContent())
                .andDo(print())
                .andDo(document("delete-dashboard-actual"));
    }

    @Test
    @Order(13)
    @DisplayName("대시보드 삭제: 잘못된 요청")
    void deleteDashboard_400_actual() throws Exception {

        mockMvc.perform(delete("/dashboards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(""))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(document("delete-dashboard-400-actual"));
    }
}
