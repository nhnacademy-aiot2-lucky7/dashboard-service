package com.nhnacademy.dashboard.dto.response;

import lombok.Getter;

@Getter
public class GrafanaChartResponse {

    private final int id;
    private final String slug;
    private final String status;
    private final String uid;
    private final String url;
    private final int version;

    public GrafanaChartResponse(int id, String slug, String status, String uid, String url, int version) {
        this.id = id;
        this.slug = slug;
        this.status = status;
        this.uid = uid;
        this.url = url;
        this.version = version;
    }
}
