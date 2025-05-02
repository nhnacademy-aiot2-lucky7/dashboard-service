package com.nhnacademy.dashboard.dto.grafanadto.dashboarddto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Dashboard {
    private int id;
    private String title;
    private String uid;
    private List<Panel> panels;
    private int schemaVersion;
    private int version;

    public Dashboard(String dashboardTitle, List<Panel> panels) {
        this.id = 0;
        this.uid = null;
        this.title = dashboardTitle;
        this.panels = panels;
        this.schemaVersion = 41;
        this.version = 0;
    }
}
