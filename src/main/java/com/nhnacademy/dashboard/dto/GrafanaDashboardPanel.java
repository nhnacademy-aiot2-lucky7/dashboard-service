package com.nhnacademy.dashboard.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class GrafanaDashboardPanel {

    private Dashboard dashboard;

    @Getter
    public static class Dashboard {
        private String title;
        private List<GrafanaPanel> panels;
    }
}
