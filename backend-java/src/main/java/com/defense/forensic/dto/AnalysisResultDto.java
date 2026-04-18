package com.defense.forensic.dto;

import java.time.Instant;

public record AnalysisResultDto(
        Long id,
        String storageKey,
        String predictionLabel,
        Double confidence,
        String imageSha256,
        String resultSha256,
        Instant createdAt
) {}
