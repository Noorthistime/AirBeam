package com.project.p2p.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Value("${file.storage.path}")
    private String storagePath;

    public String getStoragePath() {
        return storagePath;
    }
}