package com.nhnacademy.dashboard.dto.panel;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IframePanelResponse {
    private String dashboardUid;
    private String dashboardTitle;
    private int panelId;
    private long now;
    private long from;

    private IframePanelResponse(String dashboardUid, String dashboardTitle, int panelId, long from) {
        this.dashboardUid = dashboardUid;
        this.dashboardTitle = dashboardTitle;
        this.panelId = panelId;
        this.now = System.currentTimeMillis();
        this.from = now - from;
    }

    private IframePanelResponse(String dashboardUid, String dashboardTitle, int panelId) {
        this.dashboardUid = dashboardUid;
        this.dashboardTitle = dashboardTitle;
        this.panelId = panelId;
        this.now = System.currentTimeMillis();
        this.from = now - (1000L * 60 * 60); // 기본값: 1시간 전
    }

    public static IframePanelResponse ofNewIframeResponse(String uid, String dashboardTitle, int panelId, long from) {
        return new IframePanelResponse(uid, dashboardTitle, panelId, from);
    }

    public static IframePanelResponse ofNewIframeResponse(String uid, String dashboardTitle, int panelId) {
        return new IframePanelResponse(uid, dashboardTitle, panelId);
    }
}