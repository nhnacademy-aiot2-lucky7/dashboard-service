package com.nhnacademy.dashboard.dto.grafana;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SensorFieldRequestDto {

    /**
     * 측정값에 포함될 필드들의 목록 (예: activity, humidity, battery)
     */
    private String field;

    /**
     * 측정값에 포함될 gateway_id
     */
    private String gatewayId;

    /**
     * 측정값에 포함될 sensor_id
     */
    private String sensorId;
}
