package com.defense.forensic.service;

import com.defense.forensic.dto.AnalysisResultDto;
import com.defense.forensic.dto.PredictionResponse;
import com.defense.forensic.entity.AnalysisRequest;
import com.defense.forensic.entity.User;
import com.defense.forensic.repository.AnalysisRequestRepository;
import com.defense.forensic.repository.UserRepository;
import com.defense.forensic.util.Sha256Util;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class AnalysisService {
    private final UserRepository userRepository;
    private final AnalysisRequestRepository analysisRequestRepository;
    private final StorageService storageService;
    private final MlClient mlClient;

    public AnalysisService(UserRepository userRepository,
                           AnalysisRequestRepository analysisRequestRepository,
                           StorageService storageService,
                           MlClient mlClient) {
        this.userRepository = userRepository;
        this.analysisRequestRepository = analysisRequestRepository;
        this.storageService = storageService;
        this.mlClient = mlClient;
    }

    @Transactional
    public AnalysisResultDto processUpload(String email, MultipartFile file) {
        try {
            User user = userRepository.findByEmail(email).orElseThrow();
            byte[] bytes = file.getBytes();
            String storageKey = storageService.store(bytes, file.getOriginalFilename());
            PredictionResponse response = mlClient.predict(file);

            String imageHash = Sha256Util.hashBytes(bytes);
            String resultHash = Sha256Util.hashString(response.label() + ":" + response.confidence());

            AnalysisRequest entity = new AnalysisRequest();
            entity.setUser(user);
            entity.setStorageKey(storageKey);
            entity.setPredictionLabel(response.label());
            entity.setConfidence(response.confidence());
            entity.setImageSha256(imageHash);
            entity.setResultSha256(resultHash);
            analysisRequestRepository.save(entity);

            return toDto(entity);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read upload", e);
        }
    }

    public List<AnalysisResultDto> findHistory(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return analysisRequestRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private AnalysisResultDto toDto(AnalysisRequest entity) {
        return new AnalysisResultDto(
                entity.getId(),
                entity.getStorageKey(),
                entity.getPredictionLabel(),
                entity.getConfidence(),
                entity.getImageSha256(),
                entity.getResultSha256(),
                entity.getCreatedAt()
        );
    }
}
