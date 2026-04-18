package com.defense.forensic.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "analysis_requests")
public class AnalysisRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String storageKey;

    @Column(nullable = false)
    private String predictionLabel;

    @Column(nullable = false)
    private Double confidence;

    @Column(nullable = false)
    private String imageSha256;

    @Column(nullable = false)
    private String resultSha256;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public String getPredictionLabel() { return predictionLabel; }
    public void setPredictionLabel(String predictionLabel) { this.predictionLabel = predictionLabel; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public String getImageSha256() { return imageSha256; }
    public void setImageSha256(String imageSha256) { this.imageSha256 = imageSha256; }
    public String getResultSha256() { return resultSha256; }
    public void setResultSha256(String resultSha256) { this.resultSha256 = resultSha256; }
    public Instant getCreatedAt() { return createdAt; }
}
