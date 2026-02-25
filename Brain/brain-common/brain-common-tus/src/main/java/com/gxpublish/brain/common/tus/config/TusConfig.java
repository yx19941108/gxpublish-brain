package com.gxpublish.brain.common.tus.config;

import me.desair.tus.server.TusFileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class TusConfig {

    @Value("${tus.upload.dir:./data/tus-uploads}")
    private String tusUploadDir;

    @Value("${tus.upload.uri:/tus/upload}")
    private String tusUploadUri;

    @Bean
    public TusFileUploadService tusFileUploadService() {
        return new TusFileUploadService()
                .withStoragePath(tusUploadDir)
                .withDownloadFeature()
                .withUploadExpirationPeriod(1000 * 60 * 60 * 24 * 7L) // 7 days expiration for unfinished chunk uploads
                .withUploadUri(tusUploadUri);
    }
}
