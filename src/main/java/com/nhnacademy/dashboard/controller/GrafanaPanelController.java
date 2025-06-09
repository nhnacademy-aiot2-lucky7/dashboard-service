package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.api.RuleEngineApi;
import com.nhnacademy.dashboard.dto.panel.*;
import com.nhnacademy.dashboard.service.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/panels")
@RequiredArgsConstructor
public class GrafanaPanelController {

    private final GrafanaPanelService grafanaPanelService;
    private final RuleEngineApi ruleEngineApi;

    /**
     * 요청으로 전달된 대시보드 UID 및 제목을 기반으로 해당 차트들을 조회합니다.
     * GET /api/panels?dashboardUid=abcdefg
     *
     * @return 해당 대시보드에 포함된 차트 목록을 반환합니다.
     */
    @GetMapping("/{dashboardUid}")
    @Operation(summary = "패널 조회")
    public ResponseEntity<List<IframePanelResponse>> getPanel(
            @PathVariable String dashboardUid) {

        List<IframePanelResponse> result = grafanaPanelService.getPanel(new ReadPanelRequest(dashboardUid));
        return ResponseEntity.ok(result);
    }


    /**
     * 대시보드 UID 및 on/off 필터 값을 기준으로 필터링된 차트를 조회합니다.
     *
     * @param readFilterPanelRequest 대시보드 UID 및 필터 조건이 포함된 요청 객체
     * @return 필터 조건에 부합하는 차트 목록을 반환합니다.
     */
    @PostMapping("/filter")
    @Operation(summary = "메인페이지 on/off 필터 조회")
    public ResponseEntity<List<IframePanelResponse>> getFilterPanel(
            @RequestBody ReadPanelRequest readFilterPanelRequest,
            @RequestParam List<Integer> offPanelId
    ) {
        List<IframePanelResponse> charts = grafanaPanelService.getFilterPanel(readFilterPanelRequest.getDashboardUid(), offPanelId);
        return ResponseEntity.ok(charts);
    }


    @PostMapping
    @Operation(summary = "새로운 패널 추가")
    public ResponseEntity<Void> createPanel(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody @Valid PanelWithRuleRequest request
    ) {

        ResponseEntity<Void> response = ruleEngineApi.getRule(request.getRuleRequest());
        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Rule 요청 성공. 다음 단계로 진행합니다.");
            grafanaPanelService.createPanel(userId, request.getCreatePanelRequest());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .build();
        } else {
            log.warn("Rule 요청 실패: {}", response.getStatusCode());
            // 실패 시 502 Bad Gateway 등 적절한 상태 반환
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .build();
        }
    }

    @PutMapping
    @Operation(summary = "패널 쿼리 수정")
    public ResponseEntity<Void> updatePanel(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody @Valid UpdatePanelWithRuleRequest request
    ) {
        ResponseEntity<Void> response = ruleEngineApi.getRule(request.getRuleRequest());
        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Rule 수정 요청 성공. 다음 단계로 진행합니다.");
            grafanaPanelService.updatePanel(userId, request.getUpdatePanelRequest());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .build();
        } else {
            log.warn("Rule 수정 요청 실패: {}", response.getStatusCode());
            // 실패 시 502 Bad Gateway 등 적절한 상태 반환
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .build();
        }
    }

    @PutMapping("/priority")
    @Operation(summary = "패널 우선순위 수정")
    public ResponseEntity<Void> updatePriority(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody UpdatePanelPriorityRequest updatePriority
    ) {
        grafanaPanelService.updatePriority(userId, updatePriority);

        return ResponseEntity
                .ok().build();
    }

    @DeleteMapping
    @Operation(summary = "패널 삭제")
    public ResponseEntity<Void> deletePanel(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody PanelWithRemoveRuleRequest request) {
        ResponseEntity<Void> response = ruleEngineApi.getRule(request.getRuleRequest());
        if(response.getStatusCode().is2xxSuccessful()){
            log.info("Rule 삭제 요청 성공. 다음 단계로 진행합니다.");
            grafanaPanelService.removePanel(userId, request.getDeletePanelRequest());
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build();
        }else{
            log.warn("Rule 삭제 요청 실패: {}", response.getStatusCode());
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .build();
        }
    }
}
