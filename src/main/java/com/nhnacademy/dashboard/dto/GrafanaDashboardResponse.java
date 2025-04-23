package com.nhnacademy.dashboard.dto;

import lombok.Getter;

@Getter
public class GrafanaDashboardResponse {
    private final String title;
    private final int panelId;
    private final long now;
    private final long from;

    private GrafanaDashboardResponse(String title, int panelId) {
        this.title = title;
        this.panelId = panelId;
        this.now = System.currentTimeMillis();
        this.from = now - (1000 * 60 * 60); // 1시간 전
    }

    public static GrafanaDashboardResponse from(GrafanaPanel panel) {
        return new GrafanaDashboardResponse(panel.getTitle(), panel.getId());
    }
}