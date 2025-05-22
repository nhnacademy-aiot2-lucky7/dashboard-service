package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.dto.panel.CreatePanelRequest;
import com.nhnacademy.dashboard.dto.panel.DeletePanelRequest;
import com.nhnacademy.dashboard.dto.panel.ReadPanelRequest;
import com.nhnacademy.dashboard.dto.panel.IframePanelResponse;
import com.nhnacademy.dashboard.dto.panel.UpdatePanelPriorityRequest;
import com.nhnacademy.dashboard.dto.panel.UpdatePanelRequest;
import com.nhnacademy.dashboard.service.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/panels")
@RequiredArgsConstructor
public class GrafanaPanelController {


    private final GrafanaPanelService grafanaPanelService;

    /**
     * 요청으로 전달된 대시보드 UID 및 제목을 기반으로 해당 차트들을 조회합니다.
     *
     * @param readPanelRequest 대시보드 UID와 제목 정보가 담긴 요청 객체
     * @return 해당 대시보드에 포함된 차트 목록을 반환합니다.
     */
    @GetMapping
    @Operation(summary = "패널 조회")
    public ResponseEntity<List<IframePanelResponse>> getPanel(
            @RequestBody ReadPanelRequest readPanelRequest) {

        List<IframePanelResponse> result = grafanaPanelService.getPanel(readPanelRequest);
        return ResponseEntity.ok(result);
    }


    /**
     * 대시보드 UID 및 on/off 필터 값을 기준으로 필터링된 차트를 조회합니다.
     *
     * @param readFilterPanelRequest 대시보드 UID 및 필터 조건이 포함된 요청 객체
     * @return 필터 조건에 부합하는 차트 목록을 반환합니다.
     */
    @PostMapping("/filter")
    @Operation(summary ="메인페이지 on/off 필터 조회")
    public ResponseEntity<List<IframePanelResponse>> getFilterPanel(
            @RequestBody ReadPanelRequest readFilterPanelRequest,
            @RequestParam List<Integer> offPanelId
    ) {
        List<IframePanelResponse> charts  = grafanaPanelService.getFilterPanel(readFilterPanelRequest.getDashboardUid(), offPanelId);
        return ResponseEntity.ok(charts);
    }


    @PostMapping
    @Operation(summary = "새로운 패널 추가")
    public ResponseEntity<Void> createPanel(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody CreatePanelRequest createPanelRequest
    ) {
        grafanaPanelService.createPanel(userId, createPanelRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }


    @PutMapping
    @Operation(summary = "패널 쿼리 수정")
    public ResponseEntity<Void> updatePanel(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody UpdatePanelRequest updateRequest
    ){
        grafanaPanelService.updatePanel(userId, updateRequest);

        return ResponseEntity
                .ok().build();
    }

    @PutMapping("/priority")
    @Operation(summary = "패널 우선순위 수정")
    public ResponseEntity<Void> updatePriority(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody UpdatePanelPriorityRequest updatePriority
    ){
        grafanaPanelService.updatePriority(userId, updatePriority);

        return ResponseEntity
                .ok().build();
    }

    @DeleteMapping
    @Operation(summary = "패널 삭제")
    public ResponseEntity<Void> deletePanel(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody DeletePanelRequest deletePanelRequest){
        grafanaPanelService.removePanel(userId,deletePanelRequest);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
