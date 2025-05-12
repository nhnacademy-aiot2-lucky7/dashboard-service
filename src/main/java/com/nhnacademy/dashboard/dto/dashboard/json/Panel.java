package com.nhnacademy.dashboard.dto.dashboard.json;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Panel {
    private static final AtomicInteger idCounter = new AtomicInteger(1);

    private Integer id;
    private String type;
    private String title;
    private GridPos gridPos;
    private List<Target> targets;
    private Datasource datasource;

    public Panel(String type, String title, GridPos gridPos, List<Target> targets, Datasource datasource){
        this.id = idCounter.getAndIncrement();
        this.type = type;
        this.title = title;
        this.gridPos = gridPos;
        this.targets = targets;
        this.datasource = datasource;
    }

    public static void resetIdCounter() {
        idCounter.set(1);
    }
}
