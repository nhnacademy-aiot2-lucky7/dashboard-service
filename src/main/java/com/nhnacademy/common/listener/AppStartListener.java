package com.nhnacademy.common.listener;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.api.UserApi;
import com.nhnacademy.dashboard.dto.folder.CreateFolderRequest;
import com.nhnacademy.dashboard.dto.user.UserDepartmentResponse;
import com.nhnacademy.dashboard.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AppStartListener implements ApplicationListener<ApplicationReadyEvent> {

    private UserApi userApi;
    private GrafanaApi grafanaApi;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ResponseEntity<List<UserDepartmentResponse>> departmentResponseList = userApi.getDepartments();

        if (!departmentResponseList.getStatusCode().is2xxSuccessful()) {
            throw new NotFoundException("부서 리스트가 존재하지 않습니다.");
        }

        List<CreateFolderRequest> departmentNameList = Objects.requireNonNull(departmentResponseList.getBody()).stream()
                .map(d -> new CreateFolderRequest(d.getDepartmentName()))
                .toList();

        grafanaApi.createFolder(departmentNameList);
    }
}
