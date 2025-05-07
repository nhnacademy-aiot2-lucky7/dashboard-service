package com.nhnacademy.dashboard.dto.front_dto.delete;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeletePanelRequest {
    private String dashboardUid;
    private String chartTitle;
}
