package com.nhnacademy.dashboard.api;

import com.nhnacademy.common.config.FeignConfig;
import com.nhnacademy.dashboard.dto.rule.RuleRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "rule-engine",
        configuration = FeignConfig.class)
public interface RuleEngineApi {

    @PostMapping("/rule_engine/rules/create_rule")
    ResponseEntity<Void> getRule(@RequestBody RuleRequest ruleRequest);
}
