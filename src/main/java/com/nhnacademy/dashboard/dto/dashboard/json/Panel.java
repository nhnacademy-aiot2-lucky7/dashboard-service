package com.nhnacademy.dashboard.dto.dashboard.json;

import com.nhnacademy.common.memory.DashboardMemory;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Panel {

    private Integer id;
    @Pattern(
            regexp = "timeseries|table|heatmap|histogram|barchart|gauge|stat|piechart|logs|alertlist|dashlist|row|text",
            message = "지원하지 않는 그래프 타입입니다.")
    private String type;
    private String title;
    private String description;
    private GridPos gridPos;
    private List<Target> targets;
    private Datasource datasource;
    private FieldConfig fieldConfig;

    public static Panel of(String dashboardUid, String type, String title, String description, GridPos gridPos, List<Target> targets, FieldConfig fieldConfig) {
        return Panel.builder()
                .id(DashboardMemory.getPanels(dashboardUid).size())
                .type(type)
                .title(title)
                .description(description)
                .gridPos(gridPos)
                .targets(targets)
                .datasource(targets.getFirst().getDatasource())
                .fieldConfig(fieldConfig)
                .build();
    }
}
