package com.nhnacademy.dashboard.dto.rule;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RuleRequest {

    @JsonProperty("gateway_id")
    private Long gatewayId;

    @JsonProperty("sensor_id")
    private String sensorId;

    @JsonProperty("department_id")
    private String departmentId;

    @JsonProperty("type_en_name")
    private String dataTypeEnName;

    @JsonProperty("type_kr_name")
    private String dataTypeKrName;

    @JsonProperty("threshold_min")
    private Double thresholdMin;

    @JsonProperty("threshold_max")
    private Double thresholdMax;
}
