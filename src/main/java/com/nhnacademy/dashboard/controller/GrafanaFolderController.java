package com.nhnacademy.dashboard.controller;

import com.nhnacademy.dashboard.dto.folder.CreateFolderDepartmentIdRequest;
import com.nhnacademy.dashboard.dto.folder.FolderInfoResponse;
import com.nhnacademy.dashboard.dto.folder.UpdateFolderRequest;
import com.nhnacademy.dashboard.exception.AlreadyFolderNameException;
import com.nhnacademy.dashboard.exception.NotFoundException;
import com.nhnacademy.dashboard.service.GrafanaFolderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
    public ResponseEntity<List<FolderInfoResponse>> getFolders() {
        List<FolderInfoResponse> result = grafanaFolderService.getAllFolders();
        return ResponseEntity.ok(result);
    }

    /**
     * 폴더 이름을 수정합니다.
     *
     * @param updateFolderRequest 수정할 폴더의 정보 (기존 이름, 새 이름 포함)
     * @return 수정 성공 시 200 OK 응답
     * @throws IllegalArgumentException 새 폴더 이름이 중복되는 경우
     * @throws NotFoundException 해당하는 폴더가 존재하지 않는 경우
     */
    @PutMapping
    @Operation(summary = "폴더 이름 수정")
    public ResponseEntity<Void> updateFolder(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody UpdateFolderRequest updateFolderRequest
            ) {
        grafanaFolderService.updateFolder(userId, updateFolderRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    /**
     * 새로운 폴더를 생성합니다.
     *
     * @param departmentId 부서 ID (폴더 이름으로 사용)
     * @return 생성 성공 시 201 Created 응답
     * @throws AlreadyFolderNameException 같은 이름의 폴더가 이미 존재하는 경우
     */
    @PostMapping
    @Operation(summary = "새로운 폴더 생성")
    public ResponseEntity<Void> createFolder(
            @RequestBody @Valid CreateFolderDepartmentIdRequest departmentId
    ) {
        grafanaFolderService.createFolder(departmentId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}
