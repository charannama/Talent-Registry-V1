package com.zencube.registry.attachment.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileStorageService {
    
    /**
     * Stores the file and returns the generated storage path.
     */
    String store(MultipartFile file, String uniqueFilename);

    /**
     * Loads a file as a Resource.
     */
    Resource load(String storagePath);

    /**
     * Deletes a file from storage.
     */
    void delete(String storagePath);

    /**
     * Checks if a file exists in storage.
     */
    boolean exists(String storagePath);
}
