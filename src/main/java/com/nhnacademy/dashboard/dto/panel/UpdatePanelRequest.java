package com.nhnacademy.dashboard.dto.panel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nhnacademy.dashboard.dto.grafana.SensorFieldRequestDto;
import com.nhnacademy.dashboard.dto.dashboard.json.GridPos;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UpdatePanelRequest {
    /**
     * 대시보드 고유 식별자.
     */
    @NotBlank
    private String dashboardUid;

    /**
     * 패널의 고유 아이디.
     */
    @NotBlank
    private int panelId;

    /**
     * 패널의 새 제목.
     */
    @NotBlank
    @JsonProperty("panelTitle")
    private String panelNewTitle;

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
    @NotBlank
    private String type;

    /**
     * 데이터 집계 방식 (예: 평균, 합계 등).
     */
    @NotBlank
    private String aggregation;

    /**
     * 데이터를 가져올 시간 범위
     * 예) 1시간 전 -> 1h
     * 3일전 -> 3d
     */
    @NotBlank
    private String time;

    /**
     * 임계치 수정
     */
    @NotBlank
    private Double max;

    @NotBlank
    private Double min;

    /**
     * 데이터를 조회할 InfluxDB 버킷 이름.
     */
    @NotBlank
    @Builder.Default
    private String bucket = "team1-sensor-data";

    /**
     * 데이터를 조회할 InfluxDB 측정값(measurement) 이름 (예: sensor_data).
     */
    @Builder.Default
    private String measurement = "sensor_data";
}
