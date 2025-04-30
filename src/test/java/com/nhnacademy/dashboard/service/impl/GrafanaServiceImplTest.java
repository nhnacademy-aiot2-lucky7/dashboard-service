package com.nhnacademy.dashboard.service.impl;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.dto.GrafanaDashboard;
import com.nhnacademy.dashboard.dto.GrafanaDashboardInfo;
import com.nhnacademy.dashboard.dto.GrafanaDashboardPanel;
import com.nhnacademy.dashboard.dto.GrafanaFolder;
import com.nhnacademy.dashboard.dto.request.ChartCreateRequest;
import com.nhnacademy.dashboard.dto.request.GrafanaCreateDashboardRequest;
import com.nhnacademy.dashboard.dto.response.GrafanaDashboardResponse;
import com.nhnacademy.dashboard.dto.response.GrafanaFolderResponse;
import com.nhnacademy.dashboard.dto.response.GrafanaSimpleDashboardResponse;
import com.nhnacademy.dashboard.exception.NotFoundException;
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
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs
class GrafanaServiceImplTest {

    @Mock
    private GrafanaApi grafanaApi;

    @InjectMocks
    private GrafanaServiceImpl grafanaService;

    private GrafanaFolder grafanaFolder;
    private GrafanaDashboardInfo grafanaDashboardInfo;


}