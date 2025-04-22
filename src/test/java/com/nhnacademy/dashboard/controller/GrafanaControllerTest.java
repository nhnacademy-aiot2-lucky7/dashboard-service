package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.adapter.GrafanaAdapter;
import com.nhnacademy.dashboard.dto.GrafanaDashboardInfo;
import com.nhnacademy.dashboard.dto.GrafanaFolder;
import com.nhnacademy.dashboard.dto.GrafanaResponse;
import com.nhnacademy.dashboard.service.impl.GrafanaServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {GrafanaController.class})
class GrafanaControllerTest {

    @Mock
    private GrafanaAdapter grafanaAdapter;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    GrafanaServiceImpl grafanaService;

    @Test
    @DisplayName("모든 폴더 조회")
    void getFolders() throws Exception {
        List<GrafanaFolder> folders = List.of(new GrafanaFolder(1, "uid1", "Folder1"));
        Mockito.when(grafanaService.getAllFolders()).thenReturn(folders);

        mockMvc.perform(get("/api/folders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].uid").value("uid1"))
                .andExpect(jsonPath("$[0].title").value("Folder1"));
    }

    @Test
    @DisplayName("폴더명으로 대시보드명 조회")
    void getDashboardName() throws Exception {

        List<GrafanaDashboardInfo> dashboardInfos = new ArrayList<>();
        dashboardInfos.add(new GrafanaDashboardInfo("Dashboard1", "dashboardUid1", "uid1"));
        dashboardInfos.add(new GrafanaDashboardInfo("Dashboard2", "dashboardUid2", "uid1"));

        Mockito.when(grafanaService.getFolderUidByTitle(Mockito.anyString())).thenReturn("uid1");
        Mockito.when(grafanaService.getDashboardsInFolder(Mockito.anyString())).thenReturn(dashboardInfos);

        mockMvc.perform(get("/api/folders/Folders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Dashboard1"))
                .andExpect(jsonPath("$[1]").value("Dashboard2"));
    }

    @Test
    @DisplayName("폴더명으로 모든 대시보드 조회")
    void getIframeUrlsToFolder() throws Exception {
        List<GrafanaDashboardInfo> dashboardInfos = new ArrayList<>();
        dashboardInfos.add(new GrafanaDashboardInfo("Dashboard1", "dashboardUid1", "uid1"));
        dashboardInfos.add(new GrafanaDashboardInfo("Dashboard2", "dashboardUid2", "uid1"));

        Mockito.when(grafanaService.getFolderUidByTitle(Mockito.anyString())).thenReturn("uid1");
        Mockito.when(grafanaService.getDashboardsInFolder(Mockito.anyString())).thenReturn(dashboardInfos);

        mockMvc.perform(get("/api/folders/Folders/iframes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Dashboard1"))
                .andExpect(jsonPath("$[0].uid").value("dashboardUid1"))
                .andExpect(jsonPath("$[1].title").value("Dashboard2"))
                .andExpect(jsonPath("$[1].uid").value("dashboardUid2"));
    }

    @Test
    @DisplayName("대시보드명으로 특정 대시보드 조회")
    void getIframeUrlsToName() throws Exception {

        String name = "Dashboard1";
        String uid = "dashboardUid1";
        GrafanaResponse response = new GrafanaResponse(name, uid);

        Mockito.when(grafanaService.getDashboardNameUidByTitle(Mockito.anyString())).thenReturn(uid);

        mockMvc.perform(get("/api/Dashboard1/iframes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(response.getTitle()))
                .andExpect(jsonPath("$.uid").value(response.getUid()));
    }
}