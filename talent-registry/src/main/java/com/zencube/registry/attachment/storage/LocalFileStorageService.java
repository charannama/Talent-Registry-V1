package com.zencube.registry.attachment.storage;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    @Value("${storage.local.upload-dir:uploads}")
    private String uploadDir;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(uploadDir);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    @Override
    public String store(MultipartFile file, String uniqueFilename) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Failed to store empty file.");
            }
            Path destinationFile = this.rootLocation.resolve(Paths.get(uniqueFilename))
                    .normalize().toAbsolutePath();
                    
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                // Security check
                throw new SecurityException("Cannot store file outside current directory.");
            }
            
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    @Override
    public Resource load(String storagePath) {
        try {
            Path file = rootLocation.resolve(storagePath);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + storagePath);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + storagePath, e);
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            Path file = rootLocation.resolve(storagePath);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + storagePath, e);
        }
    }

    @Override
    public boolean exists(String storagePath) {
        Path file = rootLocation.resolve(storagePath);
        return Files.exists(file);
    }
}
