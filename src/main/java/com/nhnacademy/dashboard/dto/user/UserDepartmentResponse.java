package com.nhnacademy.dashboard.dto.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class UserDepartmentResponse {

    private String departmentId;
    private String departmentName;
}
