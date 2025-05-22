package com.nhnacademy.dashboard.service;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.api.UserApi;
import com.nhnacademy.dashboard.dto.folder.*;
import com.nhnacademy.dashboard.dto.user.UserDepartmentResponse;
import com.nhnacademy.dashboard.dto.user.UserInfoResponse;
import com.nhnacademy.dashboard.exception.AlreadyFolderNameException;
import com.nhnacademy.dashboard.exception.NotFoundException;
import com.nhnacademy.event.event.EventCreateRequest;
import com.nhnacademy.event.rabbitmq.EventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrafanaFolderService {

    private final GrafanaApi grafanaApi;
    private final EventProducer eventProducer;
    private final UserApi userApi;

    /**
     * Grafana에 등록된 모든 폴더를 조회합니다.
     *
     * @return 폴더 정보 리스트
     */
    public List<FolderInfoResponse> getAllFolders() {
        List<FolderInfoResponse> folders = grafanaApi.getAllFolders();

        log.info("전체 폴더 개수: {}", folders.size());
        return folders;
    }


    /**
     * 사용자 ID를 기반으로 해당 사용자의 부서명을 반환합니다.
     *
     * @param userId 사용자 ID
     * @return 부서 이름
     * @throws NotFoundException 사용자가 없거나 부서가 없을 경우
     */
    public UserDepartmentResponse getFolderTitle(String userId){
        UserInfoResponse userInfoResponse = userApi.getUserInfo(userId);
        log.debug("userInfo: {}", userInfoResponse.toString());

        UserDepartmentResponse department = userInfoResponse.getUserDepartment();
        log.debug("userDepartment: {}", userInfoResponse.getUserDepartment());
        if (department == null) {
            throw new NotFoundException("부서 정보를 찾을 수 없습니다: userId = " + userId);
        }

        return department;
    }

    /**
     * 폴더 제목을 기반으로 폴더 ID를 반환합니다.
     *
     * @param folderTitle 폴더 제목
     * @return 폴더 ID
     */
    public List<Integer> getFolderIdByTitle(String folderTitle) {

        Optional<FolderInfoResponse> folderInfo = getFolderByTitle(folderTitle);
        if (folderInfo.isPresent()) {
            return Collections.singletonList(folderInfo.get().getFolderId());
        } else {
            throw new NotFoundException("폴더를 찾을 수 없습니다: " + folderTitle);
        }
    }

    /**
     * 폴더 제목을 기반으로 폴더 UID를 반환합니다.
     *
     * @param folderTitle 폴더 제목
     * @return 폴더 UID
     */

    public String getFolderUidByTitle(String folderTitle) {
        Optional<FolderInfoResponse> folderInfo = getFolderByTitle(folderTitle);
        if (folderInfo.isPresent()) {
            return folderInfo.get().getFolderUid();
        } else {
            throw new NotFoundException("폴더를 찾을 수 없습니다: " + folderTitle);
        }
    }

    /**
     * 폴더 제목을 기반으로 해당 폴더 정보를 조회합니다.
     *
     * @param folderTitle 폴더 제목S
     * @return 폴더 정보
     * @throws NotFoundException 폴더가 존재하지 않을 경우
     */
    public Optional<FolderInfoResponse> getFolderByTitle(String folderTitle) {
        return grafanaApi.getAllFolders().stream()
                .filter(folder -> folderTitle.equals(folder.getFolderTitle()))
                .findFirst();
    }

    /**
     * 새로운 부서 정보를 개별 생성합니다.
     */
    public void createFolder(CreateFolderDepartmentIdRequest departmentId) {

        String departmentName = userApi.getDepartment(departmentId.getDepartmentId()).getDepartmentName();
        CreateFolderRequest createFolderRequest = new CreateFolderRequest(departmentName);

        duplicatedNameCheck(createFolderRequest.getTitle());

        grafanaApi.createFolder(createFolderRequest);

        String folderUid = getFolderUidByTitle(departmentName);
        EventCreateRequest event = new EventCreateRequest(
                "INFO",
                "폴더 생성",
                folderUid,
                departmentId.getDepartmentId(),
                LocalDateTime.now()
        );
        eventProducer.sendEvent(event);
    }

    /**
     * ADMIN
     * 기존 부서 이름을 수정합니다.
     */
    public void updateFolder(String userId, UpdateFolderRequest updateFolderRequest){
        GrafanaUpdateFolderRequest grafanaUpdateFolderRequest = new GrafanaUpdateFolderRequest(
                updateFolderRequest.getNewFolderName(), 1
        );

        duplicatedNameCheck(updateFolderRequest.getNewFolderName());
        String folderTitle = getFolderTitle(userId).getDepartmentName();

        if(folderTitle == null || folderTitle.isEmpty()){
            throw new NotFoundException(folderTitle+" 을 찾지 못했습니다");
        }
        String folderUid = getFolderUidByTitle(folderTitle);
        log.info("folderUid: {}", folderUid);
        grafanaApi.updateFolder(folderUid, grafanaUpdateFolderRequest);

        EventCreateRequest event = new EventCreateRequest(
                "INFO",
                "폴더 이름 수정",
                folderUid,
                updateFolderRequest.getDepartmentId(),
                LocalDateTime.now()
        );
        eventProducer.sendEvent(event);
    }

    private void duplicatedNameCheck(String folderTitle) {
        List<FolderInfoResponse> folderInfoResponseList = grafanaApi.getAllFolders();

        boolean isDuplicated = folderInfoResponseList.stream()
                .anyMatch(f -> f.getFolderTitle().equals(folderTitle));

        if (isDuplicated) {
            throw new AlreadyFolderNameException("이미 존재하는 폴더 이름입니다: " + folderTitle);
        }
    }
}
