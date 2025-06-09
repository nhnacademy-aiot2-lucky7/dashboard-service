package com.nhnacademy.dashboard.dto.panel;

import com.nhnacademy.dashboard.dto.rule.RuleRequest;
import lombok.Getter;

@Getter
public class UpdatePanelWithRuleRequest
{
    private UpdatePanelRequest updatePanelRequest;
    private RuleRequest ruleRequest;
}
