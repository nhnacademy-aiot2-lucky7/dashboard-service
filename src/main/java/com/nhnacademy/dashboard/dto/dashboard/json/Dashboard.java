package com.nhnacademy.dashboard.dto.dashboard.json;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Dashboard {
    private Integer id;
    private String title;
    private String uid;
    private List<Panel> panels;
    private int schemaVersion;
    private int version;

    public Dashboard(int id, String dashboardTitle,String uid, List<Panel> panels) {
        this.id = id;
        this.title = dashboardTitle;
        this.uid = uid;
        this.panels = panels;
        this.schemaVersion = 41;
        this.version = 0;
    }

    public Dashboard(String dashboardTitle, List<Panel> panels){
        this.id = null;
        this.title = dashboardTitle;
        this.uid = null;
        this.panels = panels;
        this.schemaVersion = 41;
        this.version = 0;
    }
}
