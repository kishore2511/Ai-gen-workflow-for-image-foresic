package com.defense.forensic.service;

import com.defense.forensic.entity.User;
import com.defense.forensic.repository.AnalysisRequestRepository;
import com.defense.forensic.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AnalysisRequestRepository analysisRequestRepository;
    @Mock
    private StorageService storageService;
    @Mock
    private MlClient mlClient;

    private AnalysisService analysisService;

    @BeforeEach
    void setUp() {
        analysisService = new AnalysisService(userRepository, analysisRequestRepository, storageService, mlClient);
        User user = new User();
        user.setEmail("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    }

    @Test
    void processUploadRejectsNonImage() {
        MockMultipartFile file = new MockMultipartFile("file", "sample.txt", "text/plain", "hello".getBytes());

        assertThrows(IllegalStateException.class, () -> analysisService.processUpload("user@example.com", file));
    }

    @Test
    void processUploadRejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "sample.png", "image/png", new byte[0]);

        assertThrows(IllegalStateException.class, () -> analysisService.processUpload("user@example.com", file));
    }
}
