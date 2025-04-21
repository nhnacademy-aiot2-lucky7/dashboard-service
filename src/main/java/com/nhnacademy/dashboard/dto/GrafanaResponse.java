package com.nhnacademy.dashboard.dto;

import lombok.Getter;

@Getter
public class GrafanaResponse {

    private final String title;
    private final String uid;
    private final long now;
    private final long from;

    public GrafanaResponse(String title, String uid) {
        this.title = title;
        this.uid = uid;
        this.now = System.currentTimeMillis();
        this.from = now - (1000 * 60 * 60);
    }
}
