package com.nhnacademy.dashboard.dto.panel;

import com.nhnacademy.dashboard.dto.grafana.SensorFieldRequestDto;
import com.nhnacademy.dashboard.dto.dashboard.json.GridPos;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CreatePanelRequest {

    /**
     * 대시보드의 고유 식별자.
     */
    private String dashboardUid;

    /**
     * 패널의 아이디.
     */
    private Integer panelId;


    /**
     * 측정하려는 데이터의 종류 모음 dto(field, gatewayId, sensorId)
     */
    private List<SensorFieldRequestDto> sensorFieldRequestDto;

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
