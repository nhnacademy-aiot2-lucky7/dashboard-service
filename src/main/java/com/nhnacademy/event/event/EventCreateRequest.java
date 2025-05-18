package com.nhnacademy.event.event;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventCreateRequest {

    @NotBlank(message = "이벤트 레벨은 필수 입력 항목입니다.")
    private String eventLevel;

    @NotBlank(message = "이벤트 내용은 필수 입력 항목입니다.")
    private String eventDetails;

    @NotBlank(message = "이벤트 출처 아이디는 필수 입력 항목입니다.")
    private String sourceId;

    @NotBlank(message = "부서 아이디는 필수 입력 항목입니다.")
    private String departmentId;

    @NotBlank(message = "이벤트 발생 일자는 필수 입력 항목입니다.")
    private LocalDateTime eventAt;
}