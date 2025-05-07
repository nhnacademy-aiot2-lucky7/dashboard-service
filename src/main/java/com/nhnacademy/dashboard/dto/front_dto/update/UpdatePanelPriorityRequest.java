package com.nhnacademy.dashboard.dto.front_dto.update;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdatePanelPriorityRequest {
    private String dashboardUid;
    private List<Integer> dashboardPriority;
}
