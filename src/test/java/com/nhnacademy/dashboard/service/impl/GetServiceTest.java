package com.nhnacademy.dashboard.service.impl;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.service.GetService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;

@Slf4j
@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs
class GetServiceTest {

    @Mock
    private GrafanaApi grafanaApi;

    @InjectMocks
    private GetService grafanaService;


}