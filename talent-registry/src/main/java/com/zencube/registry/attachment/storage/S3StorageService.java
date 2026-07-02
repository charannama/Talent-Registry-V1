package com.zencube.registry.attachment.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
public class S3StorageService implements FileStorageService {

    // AWS credentials and S3Client will be injected here when S3 is enabled

    @Override
    public String store(MultipartFile file, String uniqueFilename) {
        log.info("S3 Provider is active. Storing file {} to S3 bucket.", uniqueFilename);
        // TODO: Implement AWS SDK putObject
        throw new UnsupportedOperationException("S3 Storage is not yet fully configured.");
    }

    @Override
    public Resource load(String storagePath) {
        log.info("Loading file {} from S3 bucket.", storagePath);
        // TODO: Implement AWS SDK getObject
        throw new UnsupportedOperationException("S3 Storage is not yet fully configured.");
    }

    @Override
    public void delete(String storagePath) {
        log.info("Deleting file {} from S3 bucket.", storagePath);
        // TODO: Implement AWS SDK deleteObject
        throw new UnsupportedOperationException("S3 Storage is not yet fully configured.");
    }

    @Override
    public boolean exists(String storagePath) {
        log.info("Checking existence of file {} in S3 bucket.", storagePath);
        // TODO: Implement AWS SDK headObject
        throw new UnsupportedOperationException("S3 Storage is not yet fully configured.");
    }
}
