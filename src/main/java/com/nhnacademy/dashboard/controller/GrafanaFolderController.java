package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.dto.folder.FolderInfoResponse;
import com.nhnacademy.dashboard.exception.NotFoundException;
import com.nhnacademy.dashboard.service.GrafanaFolderService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class GrafanaFolderController {

    private final GrafanaFolderService grafanaFolderService;

    /**
     * 모든 폴더를 조회하는 API.
     *
     * @return 모든 폴더 목록
     * @throws NotFoundException 폴더가 없을 경우 예외 발생
     */
    @GetMapping
    @Operation(summary = "모든 폴더 조회")
    public ResponseEntity<List<FolderInfoResponse>> getFolders(){
        List<FolderInfoResponse> result = grafanaFolderService.getAllFolders();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Operation(summary = "새로운 폴더 생성")
    public ResponseEntity<Void> createFolder(
            @RequestBody String departmentId
    ){
        grafanaFolderService.createFolder(departmentId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @DeleteMapping
    @Operation(summary = "폴더 삭제")
    public ResponseEntity<Void> deleteFolder(@RequestHeader("X-User-Id") String userId){
        grafanaFolderService.removeFolder(userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
