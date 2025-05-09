package com.nhnacademy.common.listener;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.api.UserApi;
import com.nhnacademy.dashboard.dto.user.UserDepartmentResponse;
import com.nhnacademy.dashboard.exception.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class AppStartListenerTest {

    @Mock
    private UserApi userApi;

    @Mock
    private GrafanaApi grafanaApi;

    @InjectMocks
    private AppStartListener listener;

    @Test
    @DisplayName("정상 부서 목록일 때 Grafana 폴더 생성 호출")
    void onApplicationEvent(){

        List<UserDepartmentResponse> departments = List.of(
                new UserDepartmentResponse("1","개발팀"),
                new UserDepartmentResponse("2","기획팀")
        );

        Mockito.when(userApi.getDepartments()).thenReturn(departments);

        listener.onApplicationEvent(Mockito.mock(ApplicationReadyEvent.class));

        Mockito.verify(grafanaApi, Mockito.times(1)).createFolder(Mockito.anyList());
    }

    @Test
    @DisplayName("부서 목록이 null 인 경우 예외 발생")
    void onApplicationEvent_fail() {

        Mockito.when(userApi.getDepartments()).thenReturn(null);

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () ->
                listener.onApplicationEvent(Mockito.mock(ApplicationReadyEvent.class))
        );

        Assertions.assertEquals("부서 리스트가 존재하지 않습니다.", exception.getMessage());
    }
}