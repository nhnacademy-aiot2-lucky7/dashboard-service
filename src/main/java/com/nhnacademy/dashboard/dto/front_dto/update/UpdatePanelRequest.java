package com.nhnacademy.dashboard.dto.front_dto.update;

import com.nhnacademy.dashboard.dto.grafana_dto.dashboard_dto.GridPos;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UpdatePanelRequest {
    /**
     * 대시보드 고유 식별자.
     */
    private String dashboardUid;

    /**
     * 패널의 제목.
     */
    private String panelTitle;

    /**
     * 패널의 새 제목.
     */
    private String panelNewTitle;

    /**
     * 측정하려는 데이터의 이름 (예: sensor-data)
     */
    private String measurement;

    /**
     * 측정값에 포함될 필드들의 목록 (예: activity, humidity, battery)
     */
    private List<String> field;

    /**
     * 측정값에 포함될 gateway_id
     */
    private List<String> gatewayId;

    /**
     * 측정값에 포함될 sensor_id
     */
    private List<String> sensorId;

    /**
     * 패널의 위치 정보.
     */
    private GridPos gridPos;

    /**
     * 패널의 타입 (예: 그래프, 테이블 등).
     */
    private String type;

    /**
     * 데이터 집계 방식 (예: 평균, 합계 등).
     */
    private String aggregation;

    /**
     * 데이터를 가져올 시간 범위 (예: 1시간, 1일 등).
     */
    private String time;

}
