package com.nhnacademy.dashboard.dto.dashboard.json;

import com.nhnacademy.common.memory.DashboardMemory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Panel {

    private Integer id;
    private String type;
    private String title;
    private GridPos gridPos;
    private List<Target> targets;
    private Datasource datasource;

    public Panel(String dashboardUid, String type, String title, GridPos gridPos, List<Target> targets, Datasource datasource){
        this.id = DashboardMemory.getPanels(dashboardUid).size();
        this.type = type;
        this.title = title;
        this.gridPos = gridPos;
        this.targets = targets;
        this.datasource = datasource;
    }
}
