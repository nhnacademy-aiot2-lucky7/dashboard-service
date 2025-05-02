package com.nhnacademy.dashboard.dto.grafanadto.dashboarddto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Target {
    private String refId;
    private Datasource datasource;
    private String query;
    private String queryType;
    private String resultFormat;

    public Target(Datasource datasource, String query){
        this.refId = "A";
        this.datasource = datasource;
        this.query = query;
        this.queryType = "flux";
        this.resultFormat = "time_series";
    }
}
