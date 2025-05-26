package com.nhnacademy.dashboard.dto.dashboard;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceResponse {

    private int id;
    private int orgId;
    private String uid;
    private String name;
    private String type;
    private String typeLogoUrl;
    private String access;
    private String url;
    private String password;
    private String user;
    private String database;
    private boolean basicAuth;
    private boolean isDefault;
    private JsonData jsonData;
    private boolean readOnly;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JsonData {
        private int esVersion;
        private String logLevelField;
        private String logMessageField;
        private int maxConcurrentShardRequests;
        private String timeField;
    }
}
