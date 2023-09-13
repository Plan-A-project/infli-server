package com.plana.infli.service;

import static com.amazonaws.services.s3.model.CannedAccessControlList.*;
import static org.springframework.util.StringUtils.*;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.coobird.thumbnailator.Thumbnails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Uploader {

    private static final String BUCKET = "inflibucket";

    private static final String CLOUDFRONT_DOMAIN_NAME = "https://d1mb3nk8d8uoc7.cloudfront.net/";

    private final AmazonS3 amazonS3;

    public String uploadAsOriginalImage(MultipartFile multipartFile, String directoryPath) {

        String storeFileName = generateStoreFileName(multipartFile.getOriginalFilename());

        File file = generateFile(multipartFile, storeFileName);

        String fullPathName = generateFullPath(directoryPath, storeFileName);

        uploadToS3(file, fullPathName);

        file.delete();

        return CLOUDFRONT_DOMAIN_NAME + fullPathName;
    }

    private void uploadToS3(File file, String fullPathName) {
        amazonS3.putObject(
                new PutObjectRequest(BUCKET, fullPathName, file).withCannedAcl(PublicRead));
    }

    public String uploadAsThumbnailImage(MultipartFile multipartFile, String directoryPath) {

        String storeFileName = generateStoreFileName(
                cleanPath(multipartFile.getOriginalFilename()));

        File originalFile = generateFile(multipartFile, storeFileName);

        String fullPathName = generateFullPath(directoryPath, storeFileName);

        File thumbnailFile = generateThumbnailFile(storeFileName, originalFile);

        uploadToS3(thumbnailFile, fullPathName);

        originalFile.delete();
        thumbnailFile.delete();

        return CLOUDFRONT_DOMAIN_NAME + fullPathName;
    }

    @SneakyThrows(IOException.class)
    private File generateThumbnailFile(String storeFileName, File uploadedFile) {

        File thumbnailFile = new File(storeFileName);

        thumbnailFile.createNewFile();

        Thumbnails.of(uploadedFile)
                .size(300, 300)
                .toFile(thumbnailFile);

        return thumbnailFile;
    }


    @SneakyThrows(IOException.class)
    private File generateFile(MultipartFile multipartFile, String fullPathName) {

        File file = new File(fullPathName);

        file.createNewFile();

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        }

        return file;
    }

    private String generateStoreFileName(String originalFilename) {
        String fileExtension = getFilenameExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + fileExtension;
    }

    private String generateFullPath(String directoryName, String fileName) {
        return directoryName + "/" + fileName;
    }
}
