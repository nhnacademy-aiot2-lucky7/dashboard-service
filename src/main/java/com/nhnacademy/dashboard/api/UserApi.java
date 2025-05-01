package com.nhnacademy.dashboard.api;

import com.nhnacademy.common.config.GrafanaApiConfig;
import com.nhnacademy.dashboard.dto.userdto.UserDepartment;
import com.nhnacademy.dashboard.dto.userdto.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "userAdapter",
        path = "/users",
        url = "http://grafana.luckyseven.live",
        configuration = GrafanaApiConfig.class)
public interface UserApi {

    @GetMapping("/me")
    ResponseEntity<UserInfo> getDepartmentId(@RequestHeader String id);

    @GetMapping("/departments/{id}")
    ResponseEntity<UserDepartment> getDepartmentName(@PathVariable String id);
}
