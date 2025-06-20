package com.nhnacademy.dashboard.dto.panel;

import com.nhnacademy.dashboard.dto.dashboard.json.GridPos;
import com.nhnacademy.dashboard.dto.grafana.SensorFieldRequestDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CreatePanelRequest {

    /**
     * 대시보드의 고유 식별자.
     */
    @NotBlank
    private String dashboardUid;

    /**
     * 패널의 아이디.
     */
    private Integer panelId;

    /**
     * 패널의 이름.
     */
    @NotBlank
    private String panelTitle;

    /**
     * 측정하려는 데이터의 종류 모음 dto(field, gatewayId, sensorId)
     */
    @NotBlank
    private SensorFieldRequestDto sensorFieldRequestDto;

    /**
     * 패널의 위치 정보.
     */
    @NotBlank
    private GridPos gridPos;

    /**
     * 패널의 타입 (예: 그래프, 테이블 등).
     */
    @Pattern(
            regexp = "timeseries|table|heatmap|histogram|barchart|gauge|stat|piechart|logs|alertlist|dashlist|row|text",
            message = "지원하지 않는 그래프 타입입니다.")
    @NotBlank
    private String type;

    /**
     * 데이터 집계 방식 (예: 평균, 합계 등).
     */
    @NotBlank
    private String aggregation;

    /**
     * 데이터를 가져올 시간 범위 (예: 1시간, 1일 등).
     */
    @NotBlank
    private String time;

    /**
     * 데이터 최소값 임계치
     */
    @NotBlank
    private Double min;

    /**
     * 데이터 최대값 임계치
     */
    @NotBlank
    private Double max;

    /**
     * 데이터를 조회할 InfluxDB 버킷 이름.
     */
    @Builder.Default
    private String bucket = "team1-sensor-data";

    /**
     * 데이터를 조회할 InfluxDB 측정값(measurement) 이름 (예: sensor-data).
     */
    @Builder.Default
    private String measurement = "sensor_data";
}