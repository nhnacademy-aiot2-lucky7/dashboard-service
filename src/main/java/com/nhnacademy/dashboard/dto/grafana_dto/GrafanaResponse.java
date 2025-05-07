package com.nhnacademy.dashboard.dto.grafana_dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GrafanaResponse {

    private int id;
    private String slug;
    private String status;
    private String uid;
    private String url;
    private int version;
}
