package com.nhnacademy.dashboard.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GrafanaDashboardPanel {

    private Dashboard dashboard;

    @Getter
    @Setter
    public static class Dashboard {
        private String title;
        private List<GrafanaDashboard.Panel> panels;
    }
}
