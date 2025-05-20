package com.nhnacademy.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.dashboard.dto.folder.CreateFolderDepartmentIdRequest;
import com.nhnacademy.dashboard.dto.folder.FolderInfoResponse;
import com.nhnacademy.dashboard.dto.folder.UpdateFolderRequest;
import com.nhnacademy.dashboard.service.GrafanaFolderService;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

@WebMvcTest(GrafanaFolderController.class)
@AutoConfigureMockMvc
@AutoConfigureRestDocs
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
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].uid").value("UID-1"))
                .andExpect(jsonPath("$[0].title").value("folder-1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].uid").value("UID-2"))
                .andExpect(jsonPath("$[1].title").value("folder-2"))
                .andDo(document("get-folders"));

    }

    @Test
    @DisplayName("폴더 생성")
    void createFolder() throws Exception {

        Mockito.doNothing().when(folderService).createFolder(Mockito.any(CreateFolderDepartmentIdRequest.class));

        mockMvc.perform(post("/folders")
                .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString("IT Department")))
                .andExpect(status().isCreated())
                .andDo(print())
                .andDo(document("create-folder"));

        Mockito.verify(folderService, Mockito.times(1)).createFolder(Mockito.any(CreateFolderDepartmentIdRequest.class));
    }

    @Test
    @DisplayName("폴더 이름 수정")
    void updateFolder() throws Exception {

        UpdateFolderRequest updateFolderRequest = new UpdateFolderRequest("1","new-title");
        Mockito.doNothing().when(folderService).updateFolder("user123", updateFolderRequest);

        mockMvc.perform(put("/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(new ObjectMapper().writeValueAsString(updateFolderRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("update-folder"));

        Mockito.verify(folderService, Mockito.times(1)).updateFolder(Mockito.anyString(), Mockito.any(UpdateFolderRequest.class));
    }
}