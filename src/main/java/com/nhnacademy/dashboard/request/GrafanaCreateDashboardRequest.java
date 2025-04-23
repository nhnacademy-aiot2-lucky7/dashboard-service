package com.nhnacademy.dashboard.request;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GrafanaCreateDashboardRequest {
    private final Dashboard dashboard;
    private final int folderId;
    private final boolean overwrite;

    @Getter
    public static class Dashboard {
        private final String title;
        private final List<String> tags;
        private final List<Object> panels = new ArrayList<>();

        public Dashboard(String title) {
            this.title = title;
            this.tags = new ArrayList<>();
        }

        public Dashboard(String title, List<String> tags) {
            this.title = title;
            this.tags = tags;
        }
    }

    public GrafanaCreateDashboardRequest(Dashboard dashboard, int folderId) {
        this.dashboard = dashboard;
        this.folderId = folderId;
        this.overwrite = false;
    }
}
