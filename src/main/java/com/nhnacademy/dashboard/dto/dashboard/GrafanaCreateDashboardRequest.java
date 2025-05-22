package com.nhnacademy.dashboard.dto.dashboard;

import com.nhnacademy.dashboard.dto.dashboard.json.Dashboard;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class GrafanaCreateDashboardRequest {
    private Dashboard dashboard;
    private String folderUid;
    private boolean overwrite;
}
