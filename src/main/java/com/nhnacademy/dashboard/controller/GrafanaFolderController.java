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
            @RequestBody String departmentName
    ){
        grafanaFolderService.createFolder(departmentName);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}
