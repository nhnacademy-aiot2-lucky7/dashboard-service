package com.nhnacademy.common.mapper;

public class ResultFormatMapper {

    /**
     * 주어진 panelType에 따라 적절한 resultFormat을 반환합니다.
     *
     * @param panelType 사용자가 선택한 그래프 타입 (예: "time_series", "table", "heatmap" 등)
     * @return Grafana API에서 사용하는 resultFormat 값
     */
    public static String getResultFormat(String panelType) {
        return switch (panelType) {
            case "table", "nodeGraph", "alertlist", "bargauge", "piechart" -> "table";
            case "histogram", "heatmap" -> "heatmap";
            case "logs" -> "logs";
            default -> "time_series";
        };
    }
}
