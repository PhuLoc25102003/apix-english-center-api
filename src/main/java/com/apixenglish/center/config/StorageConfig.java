package com.apixenglish.center.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import java.net.URI;

@Configuration @RequiredArgsConstructor
public class StorageConfig {
    private final StorageProperties properties;
    @Bean(destroyMethod="close") S3Client s3Client() {
        var builder=S3Client.builder().region(Region.of(properties.getRegion())).credentialsProvider(credentials())
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled("MINIO".equalsIgnoreCase(properties.getProvider())).build());
        if(StringUtils.hasText(properties.getEndpoint())) builder.endpointOverride(URI.create(properties.getEndpoint()));
        return builder.build();
    }
    @Bean(destroyMethod="close") S3Presigner s3Presigner() {
        var builder=S3Presigner.builder().region(Region.of(properties.getRegion())).credentialsProvider(credentials())
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled("MINIO".equalsIgnoreCase(properties.getProvider())).build());
        if(StringUtils.hasText(properties.getEndpoint())) builder.endpointOverride(URI.create(properties.getEndpoint()));
        return builder.build();
    }
    private AwsCredentialsProvider credentials(){
        if(StringUtils.hasText(properties.getAccessKey())&&StringUtils.hasText(properties.getSecretKey()))
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(properties.getAccessKey(),properties.getSecretKey()));
        return DefaultCredentialsProvider.create();
    }
}
