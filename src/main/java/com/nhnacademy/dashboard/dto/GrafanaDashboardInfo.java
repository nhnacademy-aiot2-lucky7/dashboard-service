package com.nhnacademy.dashboard.dto;

import lombok.Value;


public class GrafanaDashboardInfo {
    private final String title;
    private final String uid;
    private final String uri;
    private final String url;
    private final String type;

    public GrafanaDashboardInfo(String title, String uid, String uri, String url, String type) {
        this.title = title;
        this.uid = uid;
        this.uri = uri;
        this.url = url;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public String getUid() {
        return uid;
    }

    public String getUri() {
        return uri;
    }

    public String getUrl() {
        return url;
    }

    public String getType() {
        return type;
    }
}

