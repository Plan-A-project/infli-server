package com.plana.infli.utils;

import static com.amazonaws.services.s3.model.CannedAccessControlList.*;
import static com.plana.infli.exception.custom.BadRequestException.IMAGE_IS_EMPTY;
import static org.springframework.util.StringUtils.*;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.plana.infli.exception.custom.BadRequestException;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class S3Uploader {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    @Transactional
    public String uploadAsOriginalImage(MultipartFile multipartFile, String directoryPath) {

        validateUploadedFile(multipartFile);

        String storeFileName = generateStoreFileName(multipartFile.getOriginalFilename());

        File file = generateFile(multipartFile, storeFileName);

        String fullPathName = generateFullPath(directoryPath, storeFileName);

        uploadToS3(file, fullPathName);

        file.delete();

        return amazonS3Client.getUrl(bucket, fullPathName).toString();
    }

    private void uploadToS3(File file, String fullPathName) {
        amazonS3Client.putObject(
                new PutObjectRequest(bucket, fullPathName, file).withCannedAcl(PublicRead));
    }

    @SneakyThrows
    @Transactional
    public String uploadAsThumbnailImage(MultipartFile multipartFile, String directoryPath) {

        validateUploadedFile(multipartFile);

        String storeFileName = generateStoreFileName(cleanPath(multipartFile.getOriginalFilename()));

        File originalFile = generateFile(multipartFile, storeFileName);

        String fullPathName = generateFullPath(directoryPath, storeFileName);

        File thumbnailFile = generateThumbnailFile(storeFileName, originalFile);

        uploadToS3(thumbnailFile, fullPathName);

        originalFile.delete();
        thumbnailFile.delete();

        return amazonS3Client.getUrl(bucket, fullPathName).toString();
    }

    private void validateUploadedFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()
                || multipartFile.getOriginalFilename() == null) {
            throw new BadRequestException(IMAGE_IS_EMPTY);
        }
    }

    @SneakyThrows(IOException.class)
    private File generateThumbnailFile(String storeFileName, File uploadedFile) {
        File thumbnailFile = new File(storeFileName);

        Thumbnails.of(uploadedFile)
                .size(64, 64)
                .outputFormat("jpeg")
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
