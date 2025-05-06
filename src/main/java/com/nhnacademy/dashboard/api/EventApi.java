package com.nhnacademy.dashboard.api;

import com.nhnacademy.dashboard.dto.eventdto.Event;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "eventAdapter",
        path = "/events"
)
public interface EventApi {

    @PostMapping("/create")
    ResponseEntity<Void> createEvent(@RequestBody Event event);
}
