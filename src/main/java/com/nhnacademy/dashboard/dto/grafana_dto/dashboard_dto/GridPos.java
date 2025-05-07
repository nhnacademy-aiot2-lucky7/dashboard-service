package com.nhnacademy.dashboard.dto.grafana_dto.dashboard_dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GridPos {
    private int h;
    private int w;
    private int x;
    private int y;

    public GridPos(int w, int h){
        this.x = 0;
        this.y = 0;
        this.w = w;
        this.h = h;
    }

    public GridPos(int w, int h, int y){
        this.x = 0;
        this.y = y;
        this.w = w;
        this.h = h;

    }
}
