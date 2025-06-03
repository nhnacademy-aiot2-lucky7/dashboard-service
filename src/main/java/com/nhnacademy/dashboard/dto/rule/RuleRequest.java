package com.nhnacademy.dashboard.dto.rule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RuleRequest {

    private Long gatewayId;
    private String sensorId;
    private String departmentId;
    private String dataTypeEnName;
    private String dataTypeKrName;
    private Double thresholdMin;
    private Double thresholdMax;
}
