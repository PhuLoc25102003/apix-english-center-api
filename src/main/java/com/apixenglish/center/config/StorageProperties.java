package com.apixenglish.center.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    private String provider = "MINIO";
    private String endpoint;
    private String region = "us-east-1";
    private String bucket;
    private String accessKey;
    private String secretKey;
    private String publicBaseUrl;
    private int presignedUploadExpiryMinutes = 10;
    private int presignedReadExpiryMinutes = 10;
    public String getProvider(){return provider;} public void setProvider(String v){provider=v;}
    public String getEndpoint(){return endpoint;} public void setEndpoint(String v){endpoint=v;}
    public String getRegion(){return region;} public void setRegion(String v){region=v;}
    public String getBucket(){return bucket;} public void setBucket(String v){bucket=v;}
    public String getAccessKey(){return accessKey;} public void setAccessKey(String v){accessKey=v;}
    public String getSecretKey(){return secretKey;} public void setSecretKey(String v){secretKey=v;}
    public String getPublicBaseUrl(){return publicBaseUrl;} public void setPublicBaseUrl(String v){publicBaseUrl=v;}
    public int getPresignedUploadExpiryMinutes(){return presignedUploadExpiryMinutes;} public void setPresignedUploadExpiryMinutes(int v){presignedUploadExpiryMinutes=v;}
    public int getPresignedReadExpiryMinutes(){return presignedReadExpiryMinutes;} public void setPresignedReadExpiryMinutes(int v){presignedReadExpiryMinutes=v;}
}
