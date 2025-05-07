package com.nhnacademy.dashboard.service;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.api.UserApi;
import com.nhnacademy.dashboard.dto.folder.CreateFolderRequest;
import com.nhnacademy.dashboard.dto.folder.FolderInfoResponse;
import com.nhnacademy.dashboard.dto.user.UserDepartmentResponse;
import com.nhnacademy.dashboard.dto.user.UserInfoResponse;
import com.nhnacademy.dashboard.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrafanaFolderService {

    private final GrafanaApi grafanaApi;
    private final UserApi userApi;

    /**
     * Grafana에 등록된 모든 폴더를 조회합니다.
     *
     * @return 폴더 정보 리스트
     */
    public List<FolderInfoResponse> getAllFolders() {
        List<FolderInfoResponse> folders = grafanaApi.getAllFolders();

        log.info("전체 폴더: {}", folders.toString());
        return folders;
    }


    /**
     * 사용자 ID를 기반으로 해당 사용자의 부서명을 반환합니다.
     *
     * @param userId 사용자 ID
     * @return 부서 이름
     * @throws NotFoundException 사용자가 없거나 부서가 없을 경우
     */
    public String getFolderTitle(String userId){
        UserInfoResponse userInfoResponse = userApi.getUserInfo(userId).getBody();

        if(userInfoResponse == null){
            throw new NotFoundException("user 찾을 수 없습니다: "+userId);
        }
        return userInfoResponse.getUserDepartment().getDepartmentName();
    }

    /**
     * 폴더 제목을 기반으로 폴더 ID를 반환합니다.
     *
     * @param folderTitle 폴더 제목
     * @return 폴더 ID
     */
    public List<Integer> getFolderIdByTitle(String folderTitle) {
        return Collections.singletonList(getFolderByTitle(folderTitle).getFolderId());
    }

    /**
     * 폴더 제목을 기반으로 폴더 UID를 반환합니다.
     *
     * @param folderTitle 폴더 제목
     * @return 폴더 UID
     */

    public String getFolderUidByTitle(String folderTitle) {
        return getFolderByTitle(folderTitle).getFolderUid();
    }

    /**
     * 폴더 제목을 기반으로 해당 폴더 정보를 조회합니다.
     *
     * @param folderTitle 폴더 제목
     * @return 폴더 정보
     * @throws NotFoundException 폴더가 존재하지 않을 경우
     */
    public FolderInfoResponse getFolderByTitle(String folderTitle) {
        return grafanaApi.getAllFolders().stream()
                .filter(folder -> folderTitle.equals(folder.getFolderTitle()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Folder not found: " + folderTitle));
    }

    /**
     * 새로운 부서 정보를 개별 생성합니다.
     */
    public void createFolder(String departmentId) {
        ResponseEntity<UserDepartmentResponse> userInfoResponse = userApi.getDepartment(departmentId);

        if(!userInfoResponse.getStatusCode().is2xxSuccessful()){
            throw new NotFoundException("유저정보가 존재하지 않습니다.");
        }

        String departmentName = Objects.requireNonNull(userInfoResponse.getBody()).getDepartmentName();
        List<CreateFolderRequest> createFolderRequest = Collections.singletonList(new CreateFolderRequest(departmentName));
        grafanaApi.createFolder(createFolderRequest);
    }

    /**
     * 사용자의 부서 정보를 기준으로 폴더를 찾아 해당 Grafana 폴더를 삭제합니다.
     *
     * @param userId 사용자 ID
     */
    public void removeFolder(String userId) {
        String folderTitle = getFolderTitle(userId);
        String uid = getFolderUidByTitle(folderTitle);
        grafanaApi.deleteFolder(uid);
    }
}
