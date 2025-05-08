package com.nhnacademy.dashboard.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateDashboardNameRequest {
    private String dashboardUid;
    private String dashboardNewTitle;
}
