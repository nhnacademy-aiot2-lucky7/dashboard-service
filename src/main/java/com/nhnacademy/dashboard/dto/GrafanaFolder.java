package com.nhnacademy.dashboard.dto;

public class GrafanaFolder {
    private int id;
    private String uid;
    private String title;

    public GrafanaFolder(int id, String uid, String title) {
        this.id = id;
        this.uid = uid;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public String getTitle() {
        return title;
    }
}

