package com.nhnacademy.dashboard.dto.dashboard.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nhnacademy.common.mapper.ResultFormatMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Target {
    private String refId;
    private Datasource datasource;
    private String query;
    private String queryType;
    private String resultFormat;

    @JsonIgnore
    private String panelType;

    public Target(Datasource datasource, String query, String panelType){
        this.refId = "A";
        this.datasource = datasource;
        this.query = query;
        this.queryType = "flux";
        this.resultFormat = ResultFormatMapper.getResultFormat(panelType);
    }
}
