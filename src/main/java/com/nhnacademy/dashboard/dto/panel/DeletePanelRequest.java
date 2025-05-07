package com.nhnacademy.dashboard.dto.panel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeletePanelRequest {
    private String dashboardUid;
    private Integer panelId;
}
