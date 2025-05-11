package com.nhnacademy.dashboard;

import com.nhnacademy.dashboard.api.GrafanaApi;
import com.nhnacademy.dashboard.dto.folder.FolderInfoResponse;
import com.nhnacademy.dashboard.service.GrafanaFolderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GrafanaApi grafanaApi;

    @Autowired
    private GrafanaFolderService folderService;

    @Test
    @DisplayName("폴더 조회 - 200 반환")
    void getFolders_200() throws Exception {

        List<FolderInfoResponse> response = grafanaApi.getAllFolders();

        mockMvc.perform(get("/folders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(response.getFirst().getFolderId()))
                .andExpect(jsonPath("$[0].uid").value(response.getFirst().getFolderUid()))
                .andExpect(jsonPath("$[0].title").value(response.getFirst().getFolderTitle()));
    }


    @Test
    @DisplayName("폴더 생성 및 확인")
    void createFolder_actual_check() {
        String departmentName = "TEST Department1";

        folderService.createFolder(departmentName);

        List<FolderInfoResponse> response = folderService.getAllFolders();

        log.info("response:{}", response.getFirst().getFolderTitle());
        boolean found = response.stream()
                .map(FolderInfoResponse::getFolderTitle)
                .anyMatch("TEST Department"::equals);
        Assertions.assertTrue(found);
    }
}
