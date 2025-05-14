package com.nhnacademy.dashboard.api;

import com.nhnacademy.common.config.FeignConfig;
import com.nhnacademy.dashboard.dto.user.UserDepartmentResponse;
import com.nhnacademy.dashboard.dto.user.UserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(
        name = "user-service",
        configuration = FeignConfig.class)
public interface UserApi {

    @GetMapping("/users/me")
    UserInfoResponse getUserInfo(@RequestHeader("X-User-Id") String id);

    @GetMapping("/departments")
    List<UserDepartmentResponse> getDepartments();

    @GetMapping("/departments/{departmentId}")
    UserDepartmentResponse getDepartment(@PathVariable String departmentId);

}
