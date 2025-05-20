package com.nhnacademy.dashboard.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponse {

    private String userRole;
    private String userNo;
    private String userName;
    private String userEmail;
    private String userPhone;
    @JsonProperty("department")
    private UserDepartmentResponse userDepartment;
}
