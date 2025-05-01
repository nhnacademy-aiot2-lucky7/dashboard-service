package com.nhnacademy.dashboard.dto.userdto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfo {

    private String userRole;
    private String userNo;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String userDepartment;
}
