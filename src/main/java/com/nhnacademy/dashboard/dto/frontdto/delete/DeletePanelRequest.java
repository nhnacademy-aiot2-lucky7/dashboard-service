package com.nhnacademy.dashboard.dto.frontdto.delete;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeletePanelRequest {
    private String dashboardUid;
    private String chartTitle;
}
