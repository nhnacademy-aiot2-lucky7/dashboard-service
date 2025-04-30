package com.nhnacademy.dashboard.dto;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
public class GrafanaDashboard {

    private Dashboard dashboard;
    private String folderUid;
    private boolean overwrite;

    // Dashboard Class
    @Getter
    @Setter
    @ToString
    public static class Dashboard {
        private int id;
        @NonNull
        private String title;
        private String uid;
        private List<Panel> panels;
        private int schemaVersion;
        private int version;
    }

    // Panel Class
    @Getter
    @Setter
    @ToString
    public static class Panel {
        private Integer id;
        private String type;
        private String title;
        private GridPos gridPos;
        private List<Target> targets;
        private Datasource datasource;
    }


    // GridPos Class
    @Getter
    @Setter
    public static class GridPos {
        private int h;
        private int w;
        private int x;
        private int y;
    }

    // Target Class
    @Getter
    @Setter
    public static class Target {
        private String refId;
        private Datasource datasource;
        private String query;
        private String queryType;
        private String resultFormat;
    }

    // Datasource Class
    @Getter
    @Setter
    public static class Datasource {
        private String type;
        private String uid;
    }

    @Getter
    public static class Templating {
        private List<Object> list;
    }

    // Time Class
    @Getter
    public static class Time {
        private String from;
        private String to;
    }
}