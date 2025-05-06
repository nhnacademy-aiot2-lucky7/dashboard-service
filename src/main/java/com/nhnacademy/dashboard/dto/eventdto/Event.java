package com.nhnacademy.dashboard.dto.eventdto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {
    @NotBlank(message = "이벤트 레벨은 필수 입력 항목입니다.")
    private String eventLevel;

    @NotBlank(message = "이벤트 내용은 필수 입력 항목입니다.")
    private String eventDetails;

    @NotBlank(message = "이벤트 출처 아이디는 필수 입력 항목입니다.")
    private String sourceId;

    @NotBlank(message = "이벤트 출처 타입은 필수 입력 항목입니다.")
    private String sourceType;

    private String departmentId;

    @NotBlank(message = "이벤트 발생 일자는 필수 입력 항목입니다.")
    private LocalDateTime eventAt;
}
