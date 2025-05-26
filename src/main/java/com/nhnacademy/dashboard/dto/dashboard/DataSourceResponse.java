package com.nhnacademy.dashboard.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
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

    @Getter
    @Setter
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
