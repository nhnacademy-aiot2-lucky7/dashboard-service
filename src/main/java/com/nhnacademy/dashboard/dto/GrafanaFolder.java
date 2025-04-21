package com.nhnacademy.dashboard.dto;

import lombok.Getter;

@Getter
public class GrafanaFolder {
    private final int id;
    private final String uid;
    private final String title;

    public GrafanaFolder(int id, String uid, String title) {
        this.id = id;
        this.uid = uid;
        this.title = title;
    }

}

