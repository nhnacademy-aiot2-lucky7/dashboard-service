package com.nhnacademy.dashboard.dto.response;

import lombok.Getter;

@Getter
public class IdAndUidResponse {
    private final int id;
    private final String title;
    private final String uid;
    private final String folderUid;
    private final int folderId;

    public IdAndUidResponse(int id, String title, String uid, String folderUid, int folderId) {
        this.id = id;
        this.title = title;
        this.uid = uid;
        this.folderUid = folderUid;
        this.folderId = folderId;
    }
}