package com.nhnacademy.common.memory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DashboardMemory {

    // 각 dashboardUid에 대해 중복되지 않는 panelId 집합을 저장
    private static final Map<String, Set<Integer>> DASHBOARD_PANELS = new ConcurrentHashMap<>();

    private DashboardMemory() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * dashboardUid에 panelId를 추가합니다.
     * 같은 dashboardUid 내에서는 panelId가 중복되지 않습니다.
     */
    public static void addPanel(String dashboardUid, Integer panelId) {
        DASHBOARD_PANELS
                .computeIfAbsent(dashboardUid, k -> ConcurrentHashMap.newKeySet())
                .add(panelId);
    }

    /**
     * 해당 dashboardUid에 속한 모든 panelId 목록을 반환합니다.
     */
    public static Set<Integer> getPanels(String dashboardUid) {
        return DASHBOARD_PANELS.getOrDefault(dashboardUid, Collections.emptySet());
    }

    /**
     * 해당 dashboardUid에서 특정 panelId를 제거합니다.
     */
    public static void removePanel(String dashboardUid, Integer panelId) {
        Set<Integer> panelIds = DASHBOARD_PANELS.get(dashboardUid);
        if (panelIds != null) {
            panelIds.remove(panelId);
            if (panelIds.isEmpty()) {
                DASHBOARD_PANELS.remove(dashboardUid);
            }
        }
    }

    /**
     * 해당 dashboardUid에 대한 모든 panelId를 제거합니다.
     */
    public static void clearDashboard(String dashboardUid) {
        DASHBOARD_PANELS.remove(dashboardUid);
    }
}