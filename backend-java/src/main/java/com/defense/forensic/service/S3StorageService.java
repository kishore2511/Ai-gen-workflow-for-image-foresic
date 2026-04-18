package com.defense.forensic.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Instant;
import java.util.UUID;

@Service
@Profile("aws")
public class S3StorageService implements StorageService {
    private final S3Client s3Client;
    private final String bucket;

    public S3StorageService(S3Client s3Client, @Value("${app.storage.s3-bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @Override
    public String store(byte[] bytes, String originalFilename) {
        String safeName = originalFilename == null ? "image.bin" : originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String key = "uploads/" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + "-" + safeName;
        PutObjectRequest request = PutObjectRequest.builder().bucket(bucket).key(key).contentType("application/octet-stream").build();
        s3Client.putObject(request, RequestBody.fromBytes(bytes));
        return key;
    }
}
