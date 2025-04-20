package com.nhnacademy.dashboard.service;

import com.nhnacademy.dashboard.adapter.GrafanaAdapter;
import com.nhnacademy.dashboard.dto.GrafanaDashboardInfo;
import com.nhnacademy.dashboard.dto.GrafanaFolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class GrafanaServiceImpl {

    private final GrafanaAdapter grafanaAdapter;
    @Value("${grafana.api-key}")
    private String apiKey;
    private final WebClient.Builder webClientBuilder;

    public GrafanaServiceImpl(GrafanaAdapter grafanaAdapter, WebClient.Builder webClientBuilder) {
        this.grafanaAdapter = grafanaAdapter;
        this.webClientBuilder = webClientBuilder;
    }

    // 폴더명으로 UID 찾기
    public String getFolderUidByTitle(String folderTitle){
        List<GrafanaFolder> folders = grafanaAdapter.getAllFolders("Bearer " + apiKey);  // 공백 추가
        return folders.stream()
                .filter(f -> folderTitle.equals(f.getTitle()))
                .findFirst()
                .map(GrafanaFolder::getUid)
                .orElse(null);
    }

    // 폴더UID로 대시보드 목록 조회
    public List<GrafanaDashboardInfo> getDashboardsInFolder(String folderUid){
        WebClient webClient = webClientBuilder.baseUrl("http://localhost:3000")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();  // WebClient 객체 생성

        // WebClient 사용
        return webClient.get()  // GET 요청
                .uri("/api/search?folderIds=" + folderUid + "&type=dash-db")  // 요청할 URI 설정
                .retrieve()  // 응답을 받기 위한 호출
                .bodyToMono(new ParameterizedTypeReference<List<GrafanaDashboardInfo>>() {})
                .block();  // 동기 방식으로 응답 받기
    }
}