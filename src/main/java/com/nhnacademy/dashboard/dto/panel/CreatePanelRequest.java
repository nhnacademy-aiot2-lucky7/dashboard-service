package com.nhnacademy.dashboard.dto.panel;

import com.nhnacademy.dashboard.dto.dashboard.json.FieldConfig;
import com.nhnacademy.dashboard.dto.dashboard.json.GridPos;
import com.nhnacademy.dashboard.dto.grafana.SensorFieldRequestDto;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
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
     * 패널의 이름.
     */
    private String panelTitle;

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
    @Pattern(
            regexp = "timeseries|table|heatmap|histogram|barchart|gauge|stat|piechart|logs|alertlist|dashlist|row|text",
            message = "지원하지 않는 그래프 타입입니다.")
    private String type;

    /**
     * 데이터 집계 방식 (예: 평균, 합계 등).
     */
    private String aggregation;

    /**
     * 데이터를 가져올 시간 범위 (예: 1시간, 1일 등).
     */
    private String time;

    /**
     * 데이터 최소값 임계치
     */
    private Integer min;

    /**
     * 데이터 최대값 임계치
     */
    private Integer max;

    /**
     * 데이터를 조회할 InfluxDB 버킷 이름.
     */
    @Builder.Default
    private String bucket = "temporary-data-handler";

    /**
     * 데이터를 조회할 InfluxDB 측정값(measurement) 이름 (예: sensor-data).
     */
    @Builder.Default
    private String measurement = "sensor-data";
}