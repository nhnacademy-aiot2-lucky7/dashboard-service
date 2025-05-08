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

    /**
     * 조회 시작 시간을 밀리초 단위로 나타낸 값입니다.
     * 현재 시간(now)에서 이 값을 빼면 조회 범위(from ~ now)를 계산할 수 있습니다.
     */
    private long from;
}
