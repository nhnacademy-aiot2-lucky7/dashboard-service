package com.nhnacademy.dashboard.dto;

import lombok.Getter;

@Getter
public class GrafanaDashboardInfo {
    private final String title;
    private final String uid;
    private final String folderUid;

    public GrafanaDashboardInfo(String title, String uid, String folderUid) {
        this.title = title;
        this.uid = uid;
        this.folderUid = folderUid;
    }
}