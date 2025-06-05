package com.nhnacademy.common.listener;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.api.UserApi;
import com.nhnacademy.dashboard.dto.folder.CreateFolderRequest;
import com.nhnacademy.dashboard.dto.folder.FolderInfoResponse;
import com.nhnacademy.dashboard.dto.user.UserDepartmentResponse;
import com.nhnacademy.dashboard.exception.NotFoundException;
import io.micrometer.common.lang.NonNullApi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@NonNullApi
@AllArgsConstructor
public class AppStartDepartmentListener implements ApplicationListener<ApplicationReadyEvent> {

    private final UserApi userApi;
    private final GrafanaApi grafanaApi;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            List<UserDepartmentResponse> departmentResponseList = userApi.getDepartments();

            if (departmentResponseList == null || departmentResponseList.isEmpty()) {
                throw new NotFoundException("부서 리스트가 존재하지 않습니다.");
            }

            List<CreateFolderRequest> departmentNameList = Objects.requireNonNull(departmentResponseList).stream()
                    .map(d -> new CreateFolderRequest(d.getDepartmentName()))
                    .toList();

            List<FolderInfoResponse> existingFolder = grafanaApi.getAllFolders();

            for (CreateFolderRequest department : departmentNameList) {
                boolean alreadyExists = existingFolder.stream()
                        .anyMatch(folder -> folder.getFolderTitle().equalsIgnoreCase(department.getTitle()));

                if (!alreadyExists) {
                    grafanaApi.createFolder(department);
                }
            }
        } catch (Exception e) {
            log.warn("⚠️ 초기 부서 폴더 연동 실패: " + e.getMessage());
        }
    }
}
