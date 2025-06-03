package com.nhnacademy.dashboard.dto.dashboard.json;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FieldConfig {
    private Defaults defaults;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Defaults {
        private Color color;
        private Custom custom;
        private Thresholds thresholds;
    }

    @Getter @Setter @NoArgsConstructor
    public static class Color {
        private String mode = "palette-classic";
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Custom {
        private ThresholdsStyle thresholdsStyle;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ThresholdsStyle {
        /**
         * e.g., "dashed"
         */
        private String mode;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Thresholds {
        private String mode;
        private List<Step> steps;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Step {
        /**
         * e.g., "green", "red", "#EAB839"
         */
        private String color;

        /**
         * nullable, e.g., null or 45, 60
         */
        private Integer value;
    }
}