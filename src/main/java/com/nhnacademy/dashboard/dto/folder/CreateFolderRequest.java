package com.nhnacademy.dashboard.dto.folder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateFolderRequest {
    private String uid;
    private String title;

    public CreateFolderRequest(String title){
        this.uid = "";
        this.title = title;
    }
}
