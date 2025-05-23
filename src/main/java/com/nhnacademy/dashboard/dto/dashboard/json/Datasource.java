package com.nhnacademy.dashboard.dto.dashboard.json;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Datasource {
    private String type;
    private String uid;

    public Datasource(String uid) {
        this.type = "influxdb";
        this.uid = uid;
    }
}
