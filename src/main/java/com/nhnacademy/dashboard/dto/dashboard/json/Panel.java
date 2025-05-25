package com.nhnacademy.dashboard.dto.dashboard.json;

import com.nhnacademy.common.memory.DashboardMemory;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(
            regexp = "time_series|table|heatmap|histogram|barchart|gauge|stat|piechart|logs|alertlist|dashlist|row|text",
            message = "지원하지 않는 그래프 타입입니다.")
    private String type;
    private String title;
    private GridPos gridPos;
    private List<Target> targets;
    private Datasource datasource;

    public Panel(String dashboardUid, String type, String title, GridPos gridPos, List<Target> targets){
        this.id = DashboardMemory.getPanels(dashboardUid).size();
        this.type = type;
        this.title = title;
        this.gridPos = gridPos;
        this.targets = targets;
        this.datasource = targets.getFirst().getDatasource();
    }
}
