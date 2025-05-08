package com.nhnacademy.dashboard.api;

import com.nhnacademy.common.config.FeignConfig;
import com.nhnacademy.dashboard.dto.user.UserDepartmentResponse;
import com.nhnacademy.dashboard.dto.user.UserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(
        name = "USER-SERVICE",
        path = "/users",
        configuration = FeignConfig.class)
public interface UserApi {

    @GetMapping("/me")
    UserInfoResponse getUserInfo(@RequestHeader("X-User-Id") String id);

    @GetMapping("/departments")
    List<UserDepartmentResponse> getDepartments();

    @GetMapping("/{departmentId}")
    UserDepartmentResponse getDepartment(@PathVariable String departmentId);
}
