package com.nhnacademy.dashboard.api;

import com.nhnacademy.dashboard.dto.userdto.UserDepartmentResponse;
import com.nhnacademy.dashboard.dto.userdto.UserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "userAdapter",
        path = "/users")
public interface UserApi {

    @GetMapping("/me")
    ResponseEntity<UserInfoResponse> getDepartmentId(@RequestHeader String id);

    @GetMapping("/departments/{id}")
    ResponseEntity<UserDepartmentResponse> getDepartmentName(@PathVariable String id);
}
