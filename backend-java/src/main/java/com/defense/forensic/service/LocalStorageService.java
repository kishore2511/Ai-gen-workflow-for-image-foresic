package com.defense.forensic.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.UUID;

@Service
@Primary
@Profile("!aws")
public class LocalStorageService implements StorageService {
    private final Path baseDir;

    public LocalStorageService(@Value("${app.storage.local-dir:uploads}") String localDir) {
        this.baseDir = Path.of(localDir);
    }

    @Override
    public String store(byte[] bytes, String originalFilename) {
        try {
            Files.createDirectories(baseDir);
            String safeName = originalFilename == null ? "image.bin" : originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            String key = Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + "-" + safeName;
            Path target = baseDir.resolve(key);
            Files.write(target, bytes, StandardOpenOption.CREATE_NEW);
            return key;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to persist upload", e);
        }
    }
}
