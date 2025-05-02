package com.nhnacademy.dashboard.dto.grafanadto.dashboarddto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Panel {
    private Integer id;
    private String type;
    private String title;
    private GridPos gridPos;
    private List<Target> targets;
    private Datasource datasource;

    public Panel(String type, String title, GridPos gridPos, List<Target> targets, Datasource datasource){
        this.id = null;
        this.type = type;
        this.title = title;
        this.gridPos = gridPos;
        this.targets = targets;
        this.datasource = datasource;
    }
}
