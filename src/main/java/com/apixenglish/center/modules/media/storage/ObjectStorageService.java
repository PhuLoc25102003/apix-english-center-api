package com.apixenglish.center.modules.media.storage;

import com.apixenglish.center.common.exception.BusinessException;
import com.apixenglish.center.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import java.time.Duration;
import java.time.Instant;

@Service @RequiredArgsConstructor
public class ObjectStorageService {
    private final S3Client client; private final S3Presigner presigner; private final StorageProperties properties;
    public SignedUrl presignUpload(String key,String mimeType,long size){
        var put=PutObjectRequest.builder().bucket(properties.getBucket()).key(key).contentType(mimeType).contentLength(size).build();
        var req=PutObjectPresignRequest.builder().signatureDuration(Duration.ofMinutes(properties.getPresignedUploadExpiryMinutes())).putObjectRequest(put).build();
        var signed=presigner.presignPutObject(req);
        return new SignedUrl(signed.url().toString(),Instant.now().plus(Duration.ofMinutes(properties.getPresignedUploadExpiryMinutes())));
    }
    public String presignRead(String bucket,String key){
        var req=GetObjectPresignRequest.builder().signatureDuration(Duration.ofMinutes(properties.getPresignedReadExpiryMinutes()))
                .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(key).build()).build();
        return presigner.presignGetObject(req).url().toString();
    }
    public void verifyObject(String bucket,String key,String mimeType,long size){
        try {
            var head=client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
            if(head.contentLength()!=size || head.contentType()==null || !head.contentType().equalsIgnoreCase(mimeType))
                throw new BusinessException("Uploaded object metadata does not match the presigned request","UPLOAD_OBJECT_MISMATCH");
        } catch(NoSuchKeyException e){ throw new BusinessException("Uploaded object was not found","UPLOAD_OBJECT_NOT_FOUND"); }
        catch(S3Exception e){
            if(e.statusCode()==404) throw new BusinessException("Uploaded object was not found","UPLOAD_OBJECT_NOT_FOUND");
            throw new BusinessException("Object storage verification failed","OBJECT_STORAGE_UNAVAILABLE",org.springframework.http.HttpStatus.BAD_GATEWAY);
        }
    }
    public String bucket(){return properties.getBucket();}
    public record SignedUrl(String url,Instant expiresAt){}
}
