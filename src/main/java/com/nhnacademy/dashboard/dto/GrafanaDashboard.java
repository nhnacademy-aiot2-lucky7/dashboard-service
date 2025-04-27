package com.nhnacademy.dashboard.dto;

import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.util.List;

@Getter
public class GrafanaDashboard {

    private Meta meta;

    @Setter
    @Getter
    private Dashboard dashboard;

    // Meta Class
    @Getter
    public static class Meta {
        private String type;
        private boolean canSave;
        private boolean canEdit;
        private boolean canAdmin;
        private boolean canStar;
        private boolean canDelete;
        private String slug;
        private String url;
        private String expires;
        private String created;
        private String updated;
        private String updatedBy;
        private String createdBy;
        private int version;
        private boolean hasAcl;
        private boolean isFolder;
        private int folderId;
        private String folderUid;
        private String folderTitle;
        private String folderUrl;
        private boolean provisioned;
        private String provisionedExternalId;
        private AnnotationsPermissions annotationsPermissions;
        private String publicDashboardAccessToken;
        private String publicDashboardUid;
        private boolean publicDashboardEnabled;
    }

    // AnnotationsPermissions Class
    @Getter
    public static class AnnotationsPermissions {
        private Permission dashboard;
        private Permission organization;
    }

    // Permission Class
    @Getter
    public static class Permission {
        private boolean canAdd;
        private boolean canEdit;
        private boolean canDelete;
    }

    // Dashboard Class
    @Getter
    public static class Dashboard {
        private boolean editable;
        private int fiscalYearStartMonth;
        private int graphTooltip;
        private int id;
        private List<Link> links;
        private boolean liveNow;
        private List<Panel> panels;
        private String refresh;
        private int revision;
        private int schemaVersion;
        private String style;
        private List<String> tags;
        private Templating templating;
        private Time time;
        private String title;
        private String uid;
        private int version;
        private String weekStart;
    }

    // Panel Class
    @Getter
    @Setter
    public static class Panel {
        private Datasource datasource;
        private FieldConfig fieldConfig;
        private GridPos gridPos;
        private int id;
        private Options options;
        private List<Target> targets;
        private String title;
        private String type;
        private Annotations annotations;
    }

    @Getter
    public static class Annotations {
        private List<Annotation> list; // 리스트 형태로 수정
    }

    @Getter
    public static class Annotation {
        private int builtIn;
        private Datasource datasource;
        private boolean enable;
        private boolean hide;
        private String iconColor;
        private String name;
        private Target target;
        private String type;
    }


    // FieldConfig Class
    @Getter
    public static class FieldConfig {
        private Defaults defaults;
        private List<Object> mappings;
        private Thresholds thresholds;
    }

    // Defaults Class
    @Getter
    public static class Defaults {
        private Color color;
        private Custom custom;
        private String lineInterpolation;
        private int lineWidth;
        private int pointSize;
    }

    // Color Class
    @Getter
    public static class Color {
        private String mode;
    }

    // Custom Class
    @Getter
    public static class Custom {
        private boolean axisCenteredZero;
        private String axisColorMode;
    }

    // Thresholds Class
    @Getter
    public static class Thresholds {
        private String mode;
        private List<ThresholdStep> steps;
    }

    // ThresholdStep Class
    @Getter
    public static class ThresholdStep {
        private String color;
        private Integer value;
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

    // Options Class
    @Getter
    public static class Options {
        private Legend legend;
        private Tooltip tooltip;
    }

    // Legend Class
    @Getter
    public static class Legend {
        private List<String> calcs;
        private String displayMode;
        private String placement;
        private boolean showLegend;
    }

    // Tooltip Class
    @Getter
    public static class Tooltip {
        private String mode;
        private String sort;
    }

    // Target Class
    @Getter
    public static class Target {
        private Datasource datasource;
        private String refId;
    }

    // Datasource Class
    @Getter
    public static class Datasource {
        private String type;
        private String uid;
    }

    // Link Class
    @Getter
    public static class Link {
    }

    // Templating Class
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