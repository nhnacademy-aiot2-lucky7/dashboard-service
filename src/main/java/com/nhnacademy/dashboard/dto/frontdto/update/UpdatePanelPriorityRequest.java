package com.nhnacademy.dashboard.dto.frontdto.update;

import lombok.Getter;

import java.util.List;

@Getter
public class UpdatePanelPriorityRequest {
    private String dashboardUid;
    private List<Integer> dashboardPriority;
}
