package com.nhnacademy.dashboard.dto.grafana;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GrafanaMetaResponse {

    private int id;
    private String slug;
    private String status;
    private String uid;
    private String url;
    private int version;
}
