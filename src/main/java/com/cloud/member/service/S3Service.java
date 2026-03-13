package com.cloud.member.service;

import io.awspring.cloud.s3.S3Template;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Template s3Template;
    private final String bucketName = "camp-health-pposong-files"; // S3 버킷 이름 - 파일을 저장할 S3 버킷을 지정하는 상수, 모든 파일 업로드/다운로드 시 이 버킷을 사용함

    public S3Service(S3Template s3Template) {
        this.s3Template = s3Template;
    }

    public String uploadFile(MultipartFile file) throws IOException {  // MultipartFile 클라이언트가 보낸 파일
        String fileName = "profiles/" + UUID.randomUUID() + "_" + file.getOriginalFilename(); // "profiles/" S3 폴더 경로를 지정, UUID.randomUUID() 유니크한 값 중복을 방지

        s3Template.upload(bucketName, fileName, file.getInputStream());

        return fileName;
    }

    public URL getPresignedUrl(String fileName) { // resigned URL 생성 클라이언트에게 S3 이미지에 한시적 접근 권한 부여
        return s3Template.createSignedGetURL(bucketName, fileName, Duration.ofDays(7)); // 버킷이름, 파일경로, 유효기간 7일
    }

    // CloudFront 도메인 + 파일 경로 조합으로 URL 생성
    // Presigned URL 불필요 - CloudFront가 접근 제어 담당
    public String getCloudFrontUrl(String fileName) {
        return "https://d1v1uch0vgh0vj.cloudfront.net/" + fileName;
    }
}
