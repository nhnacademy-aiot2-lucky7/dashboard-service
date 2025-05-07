package com.nhnacademy.dashboard.dto.front_dto.update;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateDashboardNameRequest {
    private String dashboardUid;
    private String dashboardNewTitle;
}
