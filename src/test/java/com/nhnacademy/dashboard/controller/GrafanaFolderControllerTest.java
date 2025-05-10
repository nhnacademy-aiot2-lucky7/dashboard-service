package com.nhnacademy.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.dashboard.dto.folder.FolderInfoResponse;
import com.nhnacademy.dashboard.service.GrafanaFolderService;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

@WebMvcTest(GrafanaFolderController.class)
@AutoConfigureMockMvc
class GrafanaFolderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    GrafanaFolderService folderService;

    @Test
    @DisplayName("폴더 조회")
    void getFolders() throws Exception {

        List<FolderInfoResponse> mockResponse = new ArrayList<>();
        mockResponse.add(new FolderInfoResponse(1,"UID-1","folder-1"));
        mockResponse.add(new FolderInfoResponse(2, "UID-2", "folder-2"));

        Mockito.when(folderService.getAllFolders()).thenReturn(mockResponse);

        mockMvc.perform(get("/folders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].folderId").value(1))
                .andExpect(jsonPath("$[0].folderUid").value("UID-1"))
                .andExpect(jsonPath("$[0].folderTitle").value("folder-1"))
                .andExpect(jsonPath("$[1].folderId").value(2))
                .andExpect(jsonPath("$[1].folderUid").value("UID-2"))
                .andExpect(jsonPath("$[1].folderTitle").value("folder-2"));

    }

    @Test
    @DisplayName("폴더 생성")
    void createFolder() throws Exception {

        Mockito.doNothing().when(folderService).createFolder(Mockito.anyString());

        mockMvc.perform(post("/folders")
                .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString("IT Department")))
                .andExpect(status().isCreated())
                .andDo(print());

        Mockito.verify(folderService, Mockito.times(1)).createFolder(Mockito.anyString());
    }
}