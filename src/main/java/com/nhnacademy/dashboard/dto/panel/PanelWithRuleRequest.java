package com.nhnacademy.dashboard.dto.panel;

import com.nhnacademy.dashboard.dto.rule.RuleRequest;
import lombok.Getter;

@Getter
public class PanelWithRuleRequest {

    private CreatePanelRequest createPanelRequest;
    private RuleRequest ruleRequest;
}
