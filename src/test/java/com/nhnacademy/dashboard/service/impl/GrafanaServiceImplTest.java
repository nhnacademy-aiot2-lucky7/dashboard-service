package com.nhnacademy.dashboard.service.impl;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.dto.GrafanaDashboardInfo;
import com.nhnacademy.dashboard.dto.GrafanaFolder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@Slf4j
@ExtendWith(MockitoExtension.class)
class GrafanaServiceImplTest {

    @Mock
    private GrafanaApi grafanaAdapter;

    @InjectMocks
    private GrafanaServiceImpl grafanaService;

    private GrafanaFolder grafanaFolder;
    private GrafanaDashboardInfo grafanaDashboardInfo;

    @BeforeEach
    void setUp() {

        grafanaFolder = new GrafanaFolder(1, "folder-uid", "Sample Folder");
        grafanaDashboardInfo = new GrafanaDashboardInfo(1,"Sample Dashboard", "dashboard-uid", "folder-uid",1);
    }

    @Test
    @DisplayName("폴더명으로 UID 찾기")
    void getFolderUidByTitle() {

        Mockito.when(grafanaAdapter.getAllFolders()).thenReturn(List.of(grafanaFolder));

        String folderUid = grafanaService.getFolderUidByTitle("Sample Folder");

        Assertions.assertEquals("folder-uid", folderUid);
    }

//    @Test
//    @DisplayName("대시보드명으로 UID 찾기")
//    void getDashboardNameUidByTitle() {
//
//        Mockito.when(grafanaAdapter.searchDashboards(Mockito.anyInt(),Mockito.anyString())).thenReturn(List.of(grafanaDashboardInfo));
//
//        String dashboardUid = grafanaService.getDashboardNameUidByTitle("Sample Dashboard");
//
//        Assertions.assertEquals("dashboard-uid", dashboardUid);
//    }

    @Test
    @DisplayName("폴더UID로 대시보드 목록 조회")
    void getDashboardsInFolder() {

        Mockito.when(grafanaAdapter.searchDashboards(Mockito.anyInt(),Mockito.anyString())).thenReturn(List.of(grafanaDashboardInfo));

        List<GrafanaDashboardInfo> folderUid = grafanaService.getDashboardsInFolder(0);

        Assertions.assertEquals("dashboard-uid", folderUid.get(0).getUid());
    }
}