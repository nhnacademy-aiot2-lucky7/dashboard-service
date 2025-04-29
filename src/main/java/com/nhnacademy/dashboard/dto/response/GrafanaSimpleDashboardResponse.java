package com.nhnacademy.dashboard.dto.response;

import com.nhnacademy.dashboard.dto.GrafanaDashboard;
import lombok.Getter;

@Getter
public class GrafanaSimpleDashboardResponse {
    private final String title;
    private final int panelId;
    private final long now;
    private final long from;

    private GrafanaSimpleDashboardResponse(String title, int panelId) {
        this.title = title;
        this.panelId = panelId;
        this.now = System.currentTimeMillis();
        this.from = now - (1000 * 60 * 60); // 1시간 전
    }

    public static GrafanaSimpleDashboardResponse from(GrafanaDashboard.Panel panel) {
        return new GrafanaSimpleDashboardResponse(panel.getTitle(), panel.getId());
    }
}