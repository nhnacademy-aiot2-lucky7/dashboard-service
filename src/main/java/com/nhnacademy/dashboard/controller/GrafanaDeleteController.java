package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.dto.front_dto.delete.DeleteDashboardRequest;
import com.nhnacademy.dashboard.dto.front_dto.delete.DeletePanelRequest;
import com.nhnacademy.dashboard.service.DeleteService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GrafanaDeleteController {
    private final DeleteService deleteService;


    @DeleteMapping("/folder")
    @Operation(summary = "폴더 삭제")
    public ResponseEntity<Void> deleteFolder(@RequestHeader ("X-User-Id") String userId){
        deleteService.removeFolder(userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @DeleteMapping("/dashboard")
    @Operation(summary = "대시보드 삭제")
    public ResponseEntity<Void> deleteDashboard(@RequestBody DeleteDashboardRequest deleteDashboardRequest){
        deleteService.removeDashboard(deleteDashboardRequest);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/chart")
    @Operation(summary = "차트 삭제")
    public ResponseEntity<Void> deleteChart(
            @RequestBody DeletePanelRequest deletePanelRequest){
        deleteService.removeChart(deletePanelRequest);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
