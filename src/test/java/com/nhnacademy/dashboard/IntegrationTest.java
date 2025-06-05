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
import com.nhnacademy.dashboard.dto.folder.CreateFolderDepartmentIdRequest;
import com.nhnacademy.dashboard.dto.folder.UpdateFolderRequest;
import com.nhnacademy.dashboard.dto.grafana.SensorFieldRequestDto;
import com.nhnacademy.dashboard.dto.panel.*;
import com.nhnacademy.dashboard.dto.user.UserDepartmentResponse;
import com.nhnacademy.dashboard.dto.user.UserInfoResponse;
import com.nhnacademy.dashboard.service.GrafanaDashboardService;
import com.nhnacademy.dashboard.service.GrafanaFolderService;
import com.nhnacademy.dashboard.service.GrafanaPanelService;
import com.nhnacademy.event.event.EventCreateRequest;
import com.nhnacademy.event.rabbitmq.EventProducer;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.retry.support.RetryTemplate;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ActiveProfiles("dev")
@Import(CommonAdvice.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(RestDocumentationExtension.class)
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

    @MockitoBean
    private EventProducer event;

    private static final String USER_ID = "user123";
    private static final String HEADER_NAME = "X-User-Id";

    private final RetryTemplate retryTemplate = RetryTemplate.builder()
            .maxAttempts(5)
            .fixedBackoff(1000)
            .build();

    private void setupUserApiMock() {
        UserInfoResponse userInfoResponse = new UserInfoResponse(
                "user",
                "1",
                "kim",
                "kim@email.com",
                "010-1111-2222",
                new UserDepartmentResponse("1", "TEST Department")
        );

        UserDepartmentResponse userDepartmentResponse = new UserDepartmentResponse("1", "TEST Department");

        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);
        when(userApi.getDepartments()).thenReturn(List.of(userDepartmentResponse));
        when(userApi.getDepartment(Mockito.anyString())).thenReturn(userDepartmentResponse);
        doNothing().when(event).sendEvent(Mockito.any(EventCreateRequest.class));
    }

    @BeforeAll
    void setUp(){

        setupUserApiMock();

        CreateFolderDepartmentIdRequest departmentIdRequest = new CreateFolderDepartmentIdRequest("1");
        // 폴더 생성 (예외 발생 시 RetryTemplate 사용)
        try {
            folderService.createFolder(departmentIdRequest);
        } catch (Exception e) {
            retryTemplate.execute(context -> {
                folderService.createFolder(departmentIdRequest);
                return null;
            });
        }

        // 대시보드 생성
        dashboardService.createDashboard(USER_ID, new CreateDashboardRequest("A"));
        dashboardService.createDashboard(USER_ID, new CreateDashboardRequest("B"));

        // panel 생성
        InfoDashboardResponse infoDashboardResponse = dashboardService.getDashboardInfoRequest(USER_ID, "A");
        CreatePanelRequest panelRequest1 = CreatePanelRequest.builder()
                .dashboardUid(infoDashboardResponse.getDashboardUid())
                .panelId(null)
                .panelTitle("P-TITLE1")
                .sensorFieldRequestDto(List.of(new SensorFieldRequestDto("battery", 1L, "abc")))
                .gridPos(new GridPos(12, 8))
                .type("time_series")
                .aggregation("mean")
                .time("1d")
                .build();

        CreatePanelRequest panelRequest2 = CreatePanelRequest.builder()
                .dashboardUid(infoDashboardResponse.getDashboardUid())
                .panelId(null)
                .panelTitle("P-TITLE2")
                .sensorFieldRequestDto(List.of(new SensorFieldRequestDto("co", 2L, null)))
                .gridPos(new GridPos(12, 8))
                .type("time_series")
                .aggregation("min")
                .time("5d")
                .build();

        panelService.createPanel(USER_ID,panelRequest1);
        panelService.createPanel(USER_ID,panelRequest2);
    }

    @BeforeEach
    void method(){

        Mockito.reset(userApi);
        setupUserApiMock();
    }

    @AfterAll
    void tearDown() {
        grafanaApi.getAllFolders()
                .forEach(f -> {
                    try {
                        grafanaApi.deleteFolder(f.getFolderUid());
                        log.info("삭제할 uid:{}", f.getFolderUid());
                    } catch (Exception e) {
                        // 로그만 남기고 무시
                        log.warn("폴더 삭제 실패 (무시됨): {}", e.getMessage());
                    }
                });
    }

    @Test
    @Order(1)
    @DisplayName("폴더 이름 수정 실패 - 중복된 이름")
    void updateFolder_duplicated_fail_actual() throws Exception {
        UpdateFolderRequest request = new UpdateFolderRequest("1","TEST Department");

        mockMvc.perform(put("/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, USER_ID)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(Matchers.containsString("이미 존재하는 폴더 이름입니다: ")))
                .andDo(document("create-folder-duplicated-fail-actual"));
    }

    @Test
    @Order(2)
    @DisplayName("대시보드 생성 실패 - 중복된 이름")
    void createDashboard_duplicated_fail_actual() throws Exception {
        CreateDashboardRequest request = new CreateDashboardRequest("A");

        mockMvc.perform(post("/dashboards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, USER_ID)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(Matchers.containsString("이미 존재하는 대시보드 이름입니다: ")))
                .andDo(document("create-dashboard-duplicated-fail-actual"));
    }

    @Test
    @DisplayName("대시보드 생성 실패 - X-User-Id 헤더 누락")
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
    @Order(3)
    @DisplayName("대시보드 이름 리스트 조회")
    void getDashboardName_actual() throws Exception {

        mockMvc.perform(get("/dashboards/names")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("A"))
                .andDo(document("get-dashboard-names-actual"));
    }

    @Test
    @Order(4)
    @DisplayName("대시보드 이름 'B' → 'B-NEW123'로 수정")
    void updateDashboard_actual() throws Exception {

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
    @Order(5)
    @DisplayName("패널 필터링 조회 - offPanelId 제외")
    void getFilterPanel_actual() throws Exception {

        InfoDashboardResponse infoDashboardResponse = dashboardService.getDashboardInfoRequest(USER_ID, "A");

        ReadPanelRequest request = new ReadPanelRequest(infoDashboardResponse.getDashboardUid());

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
    @Order(6)
    @DisplayName("panelId: [동적으로 추출된 ID] 패널 수정 - 제목 및 위치 등 변경")
    void updatePanel_actual() throws Exception {
        InfoDashboardResponse infoDashboardResponse = dashboardService.getDashboardInfoRequest(USER_ID, "A");
        log.info("Dashboard UID for 'A' in updatePanel: {}", infoDashboardResponse.getDashboardUid());

        List<IframePanelResponse> panels = panelService.getPanel(
                new ReadPanelRequest(infoDashboardResponse.getDashboardUid())
        );
        int panelIdToUpdate = panels.getFirst().getPanelId();

        UpdatePanelRequest updatePanelRequest = UpdatePanelRequest.builder()
                .dashboardUid(infoDashboardResponse.getDashboardUid())
                .panelId(panelIdToUpdate)
                .panelNewTitle("update-panelTitle")
                .sensorFieldRequestDto(List.of(new SensorFieldRequestDto("co2", 3L, "abc")))
                .gridPos(new GridPos(15, 7))
                .type("histogram")
                .aggregation("min")
                .time("3d")
                .build();

        mockMvc.perform(put("/panels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, USER_ID)
                        .content(new ObjectMapper().writeValueAsString(updatePanelRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("update-panel-actual"));
    }

    @Test
    @Order(7)
    @DisplayName("패널 우선순위 수정 - 순서: 1,0")
    void updatePriority_actual() throws Exception {

        InfoDashboardResponse infoDashboardResponse = dashboardService.getDashboardInfoRequest("user123", "A");
        ReadPanelRequest request = new ReadPanelRequest(infoDashboardResponse.getDashboardUid());
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
    @Order(8)
    @DisplayName("panelId: 0 삭제")
    void deletePanel_actual() throws Exception {

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
    @Order(9)
    @DisplayName("대시보드 'A'의 패널 전체 조회")
    void getPanel_actual() throws Exception {

        InfoDashboardResponse infoDashboardResponse = dashboardService.getDashboardInfoRequest(USER_ID, "A");
        ReadPanelRequest request = new ReadPanelRequest(infoDashboardResponse.getDashboardUid());

        mockMvc.perform(get("/panels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].panelId").value(1))
                .andDo(document("get-panel-actual"));
    }

    @Test
    @DisplayName("패널 삭제 실패 - 빈 요청 본문")
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
    @Order(10)
    @DisplayName("대시보드 'B-NEW123' 삭제")
    void deleteDashboard_actual() throws Exception {

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
    @DisplayName("대시보드 삭제 실패 - 빈 요청 본문")
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
