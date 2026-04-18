package com.defense.forensic.service;

public interface StorageService {
    String store(byte[] bytes, String originalFilename);
}
