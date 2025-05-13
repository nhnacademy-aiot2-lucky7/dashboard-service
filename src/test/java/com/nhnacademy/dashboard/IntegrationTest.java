package com.nhnacademy.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.common.advice.CommonAdvice;
import com.nhnacademy.common.memory.DashboardMemory;
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
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("test")
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
    private static final String USER_ID = "user123";
    private static final String HEADER_NAME = "X-User-Id";


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

        UserDepartmentResponse userDepartmentResponse = new UserDepartmentResponse("1", "test");
        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);
        when(userApi.getDepartments()).thenReturn(List.of(userDepartmentResponse));
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
    @DisplayName("1. 폴더 생성 후 존재 여부 확인")
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
    @DisplayName("2. 대시보드 A/B 생성 및 A 존재 확인")
    void createDashboard_actual() throws Exception {

        CreateDashboardRequest request = new CreateDashboardRequest("A");
        CreateDashboardRequest request2 = new CreateDashboardRequest("B");
        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);

        mockMvc.perform(post("/dashboards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, USER_ID)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andDo(document("create-dashboard-actual"));

        List<InfoDashboardResponse> response = dashboardService.getDashboard(USER_ID);

        boolean found = response.stream()
                .map(InfoDashboardResponse::getDashboardTitle)
                .anyMatch("A"::equals);
        Assertions.assertTrue(found);

        dashboardService.createDashboard(USER_ID,request2);
    }

    @Test
    @Order(3)
    @DisplayName("3. 대시보드 생성 실패 - 중복된 이름")
    void createDashboard_duplicated_fail_actual() throws Exception {
        CreateDashboardRequest request = new CreateDashboardRequest("A");

        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);
        mockMvc.perform(post("/dashboards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, USER_ID)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(Matchers.containsString("이미 존재하는 대시보드 이름입니다: ")))
                .andDo(document("create-dashboard-duplicated-fail-actual"));
    }

    @Test
    @Order(4)
    @DisplayName("4. 대시보드 생성 실패 - X-User-Id 헤더 누락")
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
    @DisplayName("5. 대시보드 이름 리스트 조회")
    void getDashboardName_actual() throws Exception {

        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);

        mockMvc.perform(get("/dashboards/names")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("A"))
                .andDo(document("get-dashboard-names-actual"));
    }

    @Test
    @Order(6)
    @DisplayName("6. 대시보드 이름 'B' → 'B-NEW123'로 수정")
    void updateDashboard_actual() throws Exception {

        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);

        InfoDashboardResponse infoDashboardResponse = dashboardService.getDashboardInfoRequest("user123", "B");
        UpdateDashboardNameRequest updateDashboardNameRequest = new UpdateDashboardNameRequest(
                infoDashboardResponse.getDashboardUid(),
                "B-NEW123");

        mockMvc.perform(put("/dashboards/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, USER_ID)
                        .content(new ObjectMapper().writeValueAsString(updateDashboardNameRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("update-dashboard-name-actual"));
    }

    @Test
    @Order(7)
    @DisplayName("7. 패널 P-TITLE1 생성 및 P-TITLE2 직접 호출 생성")
    void createPanel_actual() throws Exception {

        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);

        InfoDashboardResponse infoDashboardResponse = dashboardService.getDashboardInfoRequest("user123", "A");
        CreatePanelRequest panelRequest = new CreatePanelRequest(
                infoDashboardResponse.getDashboardUid(),
                null,
                "P-TITLE1",
                List.of(new SensorFieldRequestDto("battery", "12345", "abc")),
                new GridPos(12, 8),
                "time_series",
                "mean",
                "1d");
        CreatePanelRequest panelRequest2 = new CreatePanelRequest(
                infoDashboardResponse.getDashboardUid(),
                null,
                "P-TITLE2",
                List.of(new SensorFieldRequestDto("co", "12345", null)),
                new GridPos(12, 8),
                "time_series",
                "min",
                "5d");

        mockMvc.perform(post("/panels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, USER_ID)
                        .content(new ObjectMapper().writeValueAsString(panelRequest)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andDo(document("create-panel-actual"));

        panelService.createPanel("user123",panelRequest2);
    }

    @Test
    @Order(8)
    @DisplayName("8. 패널 필터링 조회 - offPanelId 제외")
    void getFilterPanel_actual() throws Exception {

        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);

        InfoDashboardResponse infoDashboardResponse = dashboardService.getDashboardInfoRequest(USER_ID, "A");

        ReadPanelRequest request = new ReadPanelRequest(infoDashboardResponse.getDashboardUid(), 1L);

        mockMvc.perform(get("/panels/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request))
                        .param("offPanelId","0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].panelId").value(1))
                .andDo(print())
                .andDo(document("get-filter-panel-actual"));
    }

    @Test
    @Order(9)
    @DisplayName("9. panelId: 1 패널 수정 - 제목 및 위치 등 변경")
    void updatePanel_actual() throws Exception {

        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);

        InfoDashboardResponse infoDashboardResponse = dashboardService.getDashboardInfoRequest(USER_ID, "A");
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
                        .header(HEADER_NAME, USER_ID)
                        .content(new ObjectMapper().writeValueAsString(updatePanelRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("update-panel-actual"));
    }

    @Test
    @Order(10)
    @DisplayName("10. 패널 우선순위 수정 - 순서: 1,0")
    void updatePriority_actual() throws Exception {

        InfoDashboardResponse infoDashboardResponse = dashboardService.getDashboardInfoRequest("user123", "A");
        ReadPanelRequest request = new ReadPanelRequest(infoDashboardResponse.getDashboardUid(), 1L);
        List<IframePanelResponse> iframePanelResponseList = panelService.getPanel(request);
        log.info("panelList:{}", iframePanelResponseList.toString());
        UpdatePanelPriorityRequest updatePanelPriorityRequest = new UpdatePanelPriorityRequest(
                infoDashboardResponse.getDashboardUid(),
                List.of(1,0));

        mockMvc.perform(put("/panels/priority")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, USER_ID)
                        .content(new ObjectMapper().writeValueAsString(updatePanelPriorityRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("update-priority-actual"));

    }

    @Test
    @Order(11)
    @DisplayName("11. panelId: 0 삭제")
    void deletePanel_actual() throws Exception {

        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);

        InfoDashboardResponse infoDashboardResponse = dashboardService.getDashboardInfoRequest(USER_ID, "A");
        DeletePanelRequest deletePanelRequest = new DeletePanelRequest(infoDashboardResponse.getDashboardUid(), 0);

        mockMvc.perform(delete("/panels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, USER_ID)
                        .content(new ObjectMapper().writeValueAsString(deletePanelRequest)))
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andDo(document("delete-panel-actual"));
    }

    @Test
    @Order(12)
    @DisplayName("12. 대시보드 'A'의 패널 전체 조회")
    void getPanel_actual() throws Exception {

        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);

        InfoDashboardResponse infoDashboardResponse = dashboardService.getDashboardInfoRequest(USER_ID, "A");
        ReadPanelRequest request = new ReadPanelRequest(infoDashboardResponse.getDashboardUid(), 1L);

        mockMvc.perform(get("/panels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].panelId").value(1))
                .andDo(document("get-panel-actual"));
    }

    @Test
    @Order(13)
    @DisplayName("13. 패널 삭제 실패 - 빈 요청 본문")
    void deletePanel_400_actual() throws Exception {

        mockMvc.perform(delete("/panels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, USER_ID)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(document("delete-panel-400-actual"));
    }


    @Test
    @Order(14)
    @DisplayName("14. 대시보드 'B-NEW123' 삭제")
    void deleteDashboard_actual() throws Exception {

        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);

        InfoDashboardResponse infoDashboardResponse = dashboardService.getDashboardInfoRequest(USER_ID, "B-NEW123");
        DeleteDashboardRequest deleteDashboardRequest = new DeleteDashboardRequest(infoDashboardResponse.getDashboardUid());

        mockMvc.perform(delete("/dashboards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, USER_ID)
                        .content(new ObjectMapper().writeValueAsString(deleteDashboardRequest)))
                .andExpect(status().isNoContent())
                .andDo(print())
                .andDo(document("delete-dashboard-actual"));
    }

    @Test
    @Order(15)
    @DisplayName("15. 대시보드 삭제 실패 - 빈 요청 본문")
    void deleteDashboard_400_actual() throws Exception {

        mockMvc.perform(delete("/dashboards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, USER_ID)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(document("delete-dashboard-400-actual"));
    }
}
