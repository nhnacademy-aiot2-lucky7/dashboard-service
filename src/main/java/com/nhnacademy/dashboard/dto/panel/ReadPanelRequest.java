package com.nhnacademy.dashboard.dto.panel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReadPanelRequest {

    /**
     * Grafana 대시보드의 고유 식별자(Uid)입니다.
     * iframe 패널을 생성하거나 대시보드를 조회할 때 사용됩니다.
     */
    private String dashboardUid;
}
