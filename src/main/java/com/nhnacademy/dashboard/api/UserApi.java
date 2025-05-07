package com.nhnacademy.dashboard.api;

import com.nhnacademy.dashboard.dto.user_dto.UserDepartmentResponse;
import com.nhnacademy.dashboard.dto.user_dto.UserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(
        name = "USER-SERVICE",
        path = "/users")
public interface UserApi {

    @GetMapping("/me")
    ResponseEntity<UserInfoResponse> getDepartmentId(@RequestHeader("X-User-Id") String id);

    @GetMapping("/departments")
    ResponseEntity<List<UserDepartmentResponse>> getDepartment();

    @GetMapping("/departments/{id}")
    ResponseEntity<UserDepartmentResponse> getDepartmentName(@PathVariable String id);
}
