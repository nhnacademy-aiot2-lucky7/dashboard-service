package com.nhnacademy.dashboard.dto.panel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class IframePanelResponse {
    private String dashboardUid;
    private String dashboardTitle;
    private int panelId;
    private long now;
    private long from;
    private long w;
    private long h;

    private IframePanelResponse(String dashboardUid, String dashboardTitle, int panelId, long from, long w, long h) {
        this.dashboardUid = dashboardUid;
        this.dashboardTitle = dashboardTitle;
        this.panelId = panelId;
        this.now = System.currentTimeMillis();
        this.from = now - from;
        this.w = w;
        this.h = h;
    }

    private IframePanelResponse(String dashboardUid, String dashboardTitle, int panelId, long w, long h) {
        this.dashboardUid = dashboardUid;
        this.dashboardTitle = dashboardTitle;
        this.panelId = panelId;
        this.now = System.currentTimeMillis();
        this.from = now - (1000L * 60 * 60); // 기본값: 1시간 전
        this.w = w;
        this.h = h;
    }

    public static IframePanelResponse ofNewIframeResponse(String uid, String dashboardTitle, int panelId, long from) {
        return new IframePanelResponse(uid, dashboardTitle, panelId, from, 0, 0);
    }

    public static IframePanelResponse ofNewIframeResponse(String uid, String dashboardTitle, int panelId, long w, long h) {
        return new IframePanelResponse(uid, dashboardTitle, panelId, w, h);
    }
}