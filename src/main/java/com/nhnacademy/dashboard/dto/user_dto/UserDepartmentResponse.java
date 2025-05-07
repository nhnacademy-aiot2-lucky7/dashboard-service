package com.nhnacademy.dashboard.dto.user_dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDepartmentResponse {

    private String departmentId;
    private String departmentName;
}
