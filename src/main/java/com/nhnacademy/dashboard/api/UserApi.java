package com.nhnacademy.dashboard.api;

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
        configuration = FeignClient.class)
public interface UserApi {

    @GetMapping("/me")
    ResponseEntity<UserInfoResponse> getUserInfo(@RequestHeader("X-User-Id") String id);

    @GetMapping("/departments")
    ResponseEntity<List<UserDepartmentResponse>> getDepartments();

    @GetMapping("/{departmentId}")
    ResponseEntity<UserDepartmentResponse> getDepartment(@PathVariable String departmentId);
}
