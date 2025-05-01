package com.nhnacademy.dashboard.dto.response;

import lombok.Getter;

@Getter
public class IframeResponse {
    private final String dashboardUid;
    private final String dashboardTitle;
    private final int panelId;
    private final long now;
    private final long from;

    private IframeResponse(String dashboardUid, String dashboardTitle, int panelId) {
        this.dashboardUid = dashboardUid;
        this.dashboardTitle = dashboardTitle;
        this.panelId = panelId;
        this.now = System.currentTimeMillis();
        this.from = now - (1000 * 60 * 60); // 1시간 전
    }

    public static IframeResponse iframeResponse(String uid, String dashboardTitle, int panelId) {
        return new IframeResponse(uid, dashboardTitle, panelId);
    }
}