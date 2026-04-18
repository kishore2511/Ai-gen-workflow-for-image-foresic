package com.defense.forensic;

import com.defense.forensic.dto.PredictionResponse;
import com.defense.forensic.service.MlClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.storage.local-dir=target/test-uploads"
})
class AnalysisControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MlClient mlClient;

    @Test
    void uploadAndHistoryFlowWorksWithJwt() throws Exception {
        when(mlClient.predict(any())).thenReturn(new PredictionResponse("fake", 0.88));

        String registerBody = """
                {"email":"mltester@example.com","password":"strongpass123"}
                """;

        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(registerResponse);
        String token = json.get("token").asText();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/analysis/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.predictionLabel").value("fake"))
                .andExpect(jsonPath("$.confidence").value(0.88))
                .andExpect(jsonPath("$.imageSha256").isNotEmpty())
                .andExpect(jsonPath("$.resultSha256").isNotEmpty());

        mockMvc.perform(get("/api/analysis/history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].predictionLabel").value("fake"));
    }
}
