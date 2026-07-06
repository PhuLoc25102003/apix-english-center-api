package com.apixenglish.center.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "media")
public class MediaProperties {
    private int uploadSessionExpiryMinutes = 20;
    private int shareLinkExpiryDays = 30;
    private String uploadPageBaseUrl;
    private String publicShareBaseUrl;
    private Video video = new Video();
    public int getUploadSessionExpiryMinutes(){return uploadSessionExpiryMinutes;} public void setUploadSessionExpiryMinutes(int v){uploadSessionExpiryMinutes=v;}
    public int getShareLinkExpiryDays(){return shareLinkExpiryDays;} public void setShareLinkExpiryDays(int v){shareLinkExpiryDays=v;}
    public String getUploadPageBaseUrl(){return uploadPageBaseUrl;} public void setUploadPageBaseUrl(String v){uploadPageBaseUrl=v;}
    public String getPublicShareBaseUrl(){return publicShareBaseUrl;} public void setPublicShareBaseUrl(String v){publicShareBaseUrl=v;}
    public Video getVideo(){return video;} public void setVideo(Video v){video=v;}
    public static class Video {
        private long maxFileSizeMb = 500;
        private List<String> allowedMimeTypes = new ArrayList<>(List.of("video/mp4","video/quicktime","video/webm","video/x-matroska"));
        public long getMaxFileSizeMb(){return maxFileSizeMb;} public void setMaxFileSizeMb(long v){maxFileSizeMb=v;}
        public List<String> getAllowedMimeTypes(){return allowedMimeTypes;} public void setAllowedMimeTypes(List<String> v){allowedMimeTypes=v;}
    }
}
