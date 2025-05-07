package com.nhnacademy.dashboard.dto.front_dto.create;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CreateFolderRequest {
    private String uid;
    private String title;

    public CreateFolderRequest(String title){
        this.uid = "";
        this.title = title;
    }
}
