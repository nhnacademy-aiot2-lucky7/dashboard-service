package com.nhnacademy.dashboard.dto;

import java.util.List;

public class GrafanaDetailDashboard {

    private Dashboard dashboard;

    public Dashboard getDashboard() {
        return dashboard;
    }

    public static class Dashboard {
        private List<GrafanaPanel> panels;

        public List<GrafanaPanel> getPanels() {
            return panels;
        }
    }
}
