package com.nhnacademy.common.listener;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.api.UserApi;
import com.nhnacademy.dashboard.dto.folder.CreateFolderRequest;
import com.nhnacademy.dashboard.dto.user.UserDepartmentResponse;
import com.nhnacademy.dashboard.exception.NotFoundException;
import io.micrometer.common.lang.NonNullApi;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@NonNullApi
@Profile("!test")
@AllArgsConstructor
public class AppStartDepartmentListener implements ApplicationListener<ApplicationReadyEvent> {

    private final UserApi userApi;
    private final GrafanaApi grafanaApi;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        List<UserDepartmentResponse> departmentResponseList = userApi.getDepartments();

        if (departmentResponseList == null || departmentResponseList.isEmpty()) {
            throw new NotFoundException("부서 리스트가 존재하지 않습니다.");
        }

        List<CreateFolderRequest> departmentNameList = Objects.requireNonNull(departmentResponseList).stream()
                .map(d -> new CreateFolderRequest(d.getDepartmentName()))
                .toList();

        for(CreateFolderRequest department: departmentNameList){
            grafanaApi.createFolder(department);
        }
    }
}
