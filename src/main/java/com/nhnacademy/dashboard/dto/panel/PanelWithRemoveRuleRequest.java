package com.nhnacademy.dashboard.dto.panel;

import com.nhnacademy.dashboard.dto.rule.DeleteRuleRequest;
import lombok.Getter;

@Getter
public class PanelWithRemoveRuleRequest {
    DeletePanelRequest deletePanelRequest;
    DeleteRuleRequest ruleRequest;
}
