package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.adapter.GrafanaAdapter;
import com.nhnacademy.dashboard.dto.GrafanaFolder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GrafanaController {

    Logger log = LoggerFactory.getLogger(getClass());

    private final GrafanaAdapter grafanaAdapter;

    @Value("${grafana.api-key}")
    private String apiKey;

    public GrafanaController(GrafanaAdapter grafanaAdapter) {
        this.grafanaAdapter = grafanaAdapter;
    }

    @GetMapping("/folders")
    public List<GrafanaFolder> getFolders(){
        List<GrafanaFolder> response =grafanaAdapter.getAllFolders("Bearer " + apiKey);

        log.info("response:{}", response);
        return response;
    }

}
