package com.nhnacademy.dashboard.service;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.api.UserApi;
import com.nhnacademy.dashboard.dto.folder.CreateFolderRequest;
import com.nhnacademy.dashboard.dto.folder.FolderInfoResponse;
import com.nhnacademy.dashboard.dto.user.UserDepartmentResponse;
import com.nhnacademy.dashboard.dto.user.UserInfoResponse;
import com.nhnacademy.dashboard.exception.BadRequestException;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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

        when(grafanaApi.getAllFolders()).thenReturn(List.of(folderInfoResponse));

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
    @DisplayName("userId로 부서명 조회")
    void getFolderTitle() {

        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(userInfoResponse);
        String departmentName = folderService.getFolderTitle(Mockito.anyString());

        Assertions.assertEquals("부서명", departmentName);

    }

    @Test
    @DisplayName("userId로 부서명 조회 실패")
    void getFolderTitle_fail() {

        when(userApi.getUserInfo(Mockito.anyString())).thenReturn(Mockito.any(UserInfoResponse.class));

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, ()-> folderService.getFolderTitle("1"));
        Assertions.assertEquals("user 찾을 수 없습니다: 1", exception.getMessage());
    }

    @Test
    @DisplayName("부서명으로 폴더 ID 조회")
    void getFolderIdByTitle() {

        when(grafanaApi.getAllFolders()).thenReturn(List.of(folderInfoResponse));
        List<Integer> folderId = folderService.getFolderIdByTitle("folder-title");

        Assertions.assertNotNull(folderId);
        Assertions.assertEquals(1, folderId.getFirst());
    }

    @Test
    @DisplayName("부서명으로 폴더 ID 조회 -> 비어있는 리스트 반환")
    void getFolderIdByTitle_404() {

        when(grafanaApi.getAllFolders()).thenReturn(List.of());

        Assertions.assertThrows(NotFoundException.class, ()-> folderService.getFolderIdByTitle("folder-title"));
    }


    @Test
    @DisplayName("부서명으로 폴더 UID 조회")
    void getFolderUidByTitle() {

        when(grafanaApi.getAllFolders()).thenReturn(List.of(folderInfoResponse));
        String folderUid = folderService.getFolderUidByTitle("folder-title");

        Assertions.assertNotNull(folderUid);
        Assertions.assertEquals("folder-uid", folderUid);
    }

    @Test
    @DisplayName("부서명으로 폴더 UID 조회 -> 비어있는 리스트 반환")
    void getFolderUidByTitle_404() {

        when(grafanaApi.getAllFolders()).thenReturn(List.of());

        Assertions.assertThrows(NotFoundException.class, ()-> folderService.getFolderUidByTitle("folder-title"));
    }

    @Test
    @DisplayName("부서명으로 폴더 정보 조회")
    void getFolderByTitle() {

        when(grafanaApi.getAllFolders()).thenReturn(List.of(folderInfoResponse));
        Optional<FolderInfoResponse> folderInfoResponse1 = folderService.getFolderByTitle("folder-title");

        Assertions.assertNotNull(folderInfoResponse1);
        Assertions.assertTrue(folderInfoResponse1.isPresent());
        Assertions.assertAll(
                ()->{
                    Assertions.assertEquals(1,folderInfoResponse1.get().getFolderId());
                    Assertions.assertEquals("folder-uid",folderInfoResponse1.get().getFolderUid());
                    Assertions.assertEquals("folder-title",folderInfoResponse1.get().getFolderTitle());
                }
        );
    }

    @Test
    @DisplayName("부서명으로 폴더 생성")
    void createFolder() {

        String departmentName = "부서A";

        folderService.createFolder(departmentName);

        Mockito.verify(grafanaApi, Mockito.times(1)).createFolder(Mockito.any(CreateFolderRequest.class));
    }

    @Test
    @DisplayName("부서명으로 폴더 생성 실패 -> 중복 이름")
    void createFolder_duplicated() {

        String departmentName = "부서A";

        FolderInfoResponse infoResponse = new FolderInfoResponse(
                1,
                "folder-uid",
                "부서A"
        );
        when(grafanaApi.getAllFolders()).thenReturn(List.of(infoResponse));
        BadRequestException exception = Assertions.assertThrows(BadRequestException.class, ()-> folderService.createFolder(departmentName));

        Assertions.assertEquals("폴더 '" + departmentName + "'은 이미 존재합니다.", exception.getMessage());
    }
}