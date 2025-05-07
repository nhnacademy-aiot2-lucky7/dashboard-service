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

    // 시간 수정
    private IframePanelResponse(String dashboardUid, String dashboardTitle, int panelId) {
        this.dashboardUid = dashboardUid;
        this.dashboardTitle = dashboardTitle;
        this.panelId = panelId;
        this.now = System.currentTimeMillis();
        this.from = now - from;
    }

    public static IframePanelResponse ofNewIframeResponse(String uid, String dashboardTitle, int panelId) {
        return new IframePanelResponse(uid, dashboardTitle, panelId);
    }
}