package com.nhnacademy.dashboard.service;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.api.UserApi;
import com.nhnacademy.dashboard.dto.folder.FolderInfoResponse;
import com.nhnacademy.dashboard.dto.user.UserDepartmentResponse;
import com.nhnacademy.dashboard.dto.user.UserInfoResponse;
import com.nhnacademy.dashboard.exception.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GrafanaFolderServiceTest {

    @Mock
    private GrafanaApi grafanaApi;

    @Mock
    private UserApi userApi;

    @InjectMocks
    GrafanaFolderService folderService;

    private FolderInfoResponse folderInfoResponse;
    private UserInfoResponse userInfoResponse;

    @BeforeEach
    void setUp() {
        folderInfoResponse = new FolderInfoResponse(1, "folder-uid", "folder-title");
        userInfoResponse = new UserInfoResponse(
                "role",
                "no",
                "name",
                "email",
                "phone",
                new UserDepartmentResponse("1","부서명")
        );
    }

    @Test
    @DisplayName("모든 폴더 조회 성공")
    void getAllFolders() {

        Mockito.when(grafanaApi.getAllFolders()).thenReturn(List.of(folderInfoResponse));

        List<FolderInfoResponse> folders = folderService.getAllFolders();

        assertNotNull(folders);
        assertAll(
                ()->{
                    assertEquals(1, folders.getFirst().getFolderId());
                    assertEquals("folder-uid", folders.getFirst().getFolderUid());
                    assertEquals("folder-title", folders.getFirst().getFolderTitle());
                }
        );
    }

    @Test
    @DisplayName("모든 폴더 조회 실패")
    void getAllFolders_fail() {

        Mockito.when(grafanaApi.getAllFolders()).thenReturn(Collections.emptyList());

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, ()-> folderService.getAllFolders());
        Assertions.assertEquals("Grafana에 등록된 폴더가 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("userId로 부서명 조회")
    void getFolderTitle() {

        Mockito.when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);
        String departmentName = folderService.getFolderTitle(Mockito.anyString());

        Assertions.assertEquals("부서명", departmentName);

    }

    @Test
    @DisplayName("userId로 부서명 조회 실패")
    void getFolderTitle_fail() {

        Mockito.when(userApi.getUserInfo(Mockito.anyString())).thenReturn(Mockito.any(UserInfoResponse.class));

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, ()-> folderService.getFolderTitle("1"));
        Assertions.assertEquals("user 찾을 수 없습니다: 1", exception.getMessage());
    }

    @Test
    @DisplayName("부서명으로 폴더 ID 조회")
    void getFolderIdByTitle() {

        Mockito.when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);
        Mockito.when(grafanaApi.getAllFolders()).thenReturn(List.of(folderInfoResponse));
        List<Integer> folderId = folderService.getFolderIdByTitle("folder-title");

        Assertions.assertNotNull(folderId);
        Assertions.assertEquals(1, folderId.getFirst());
    }

    @Test
    @DisplayName("부서명으로 폴더 UID 조회")
    void getFolderUidByTitle() {

        Mockito.when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);
        Mockito.when(grafanaApi.getAllFolders()).thenReturn(List.of(folderInfoResponse));
        String folderUid = folderService.getFolderUidByTitle("folder-title");

        Assertions.assertNotNull(folderUid);
        Assertions.assertEquals("folder-uid", folderUid);
    }

    @Test
    @DisplayName("부서명으로 폴더 정보 조회")
    void getFolderByTitle() {

        Mockito.when(grafanaApi.getAllFolders()).thenReturn(List.of(folderInfoResponse));
        FolderInfoResponse folderInfoResponse1 = folderService.getFolderByTitle("folder-title");

        Assertions.assertNotNull(folderInfoResponse1);
        Assertions.assertAll(
                ()->{
                    Assertions.assertEquals(1,folderInfoResponse1.getFolderId());
                    Assertions.assertEquals("folder-uid",folderInfoResponse1.getFolderUid());
                    Assertions.assertEquals("folder-title",folderInfoResponse1.getFolderTitle());
                }
        );
    }

    @Test
    @DisplayName("부서명으로 폴더 생성")
    void createFolder() {

        String departmentName = "부서A";

        folderService.createFolder(departmentName);

        Mockito.verify(grafanaApi, Mockito.times(1)).createFolder(Mockito.anyList());
    }
}