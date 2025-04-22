package com.nhnacademy.dashboard.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class GrafanaDashboardResponse {

    private final String title;
    private final String uid;
    private final int panelIds;
    private final long now;
    private final long from;

    private GrafanaDashboardResponse(String title, String uid, int panelIds) {
        this.title = title;
        this.uid = uid;
        this.panelIds = panelIds;
        this.now = System.currentTimeMillis();
        this.from = now - (1000 * 60 * 60);
    }

    public static GrafanaDashboardResponse ofGrafanaDashboardResponse(String title, String uid, int panelIds) {
        return new GrafanaDashboardResponse(title, uid, panelIds);
    }
}
