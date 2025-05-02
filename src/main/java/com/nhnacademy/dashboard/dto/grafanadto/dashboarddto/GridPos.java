package com.nhnacademy.dashboard.dto.grafanadto.dashboarddto;

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
}
