package com.nhnacademy.dashboard.dto;

import lombok.Getter;

@Getter
public class GrafanaFolderResponse {

    private final String title;
    private final String uid;
    private final long now;
    private final long from;


    private GrafanaFolderResponse(String title, String uid) {
        this.title = title;
        this.uid = uid;
        this.now = System.currentTimeMillis();
        this.from = now - (1000 * 60 * 60);
    }

    public static GrafanaFolderResponse ofGrafanaResponse(String title, String uid) {
        return new GrafanaFolderResponse(title, uid);
    }
}
