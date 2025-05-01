package com.nhnacademy.dashboard.dto.response;

import lombok.Getter;

@Getter
public class FolderInfoResponse {
    private final int id;
    private final String uid;
    private final String title;

    public FolderInfoResponse(int id, String uid, String title) {
        this.id = id;
        this.uid = uid;
        this.title = title;
    }

}

