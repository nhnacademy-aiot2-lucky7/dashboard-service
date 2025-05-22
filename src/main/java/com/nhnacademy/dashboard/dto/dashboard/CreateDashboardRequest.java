package com.nhnacademy.dashboard.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateDashboardRequest {
    private String dashboardTitle;
}