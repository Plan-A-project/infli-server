package com.plana.infli.utils;

import static com.amazonaws.services.s3.model.CannedAccessControlList.*;
import static com.plana.infli.exception.custom.InternalServerErrorException.*;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.plana.infli.exception.custom.InternalServerErrorException;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
    public String upload(MultipartFile multipartFile, String directoryPath) {

        String storeFileName = generateStoreFileName(multipartFile.getOriginalFilename());

        String fullPathName = generateFullPath(directoryPath, storeFileName);

        File file = convertToFile(multipartFile, storeFileName);

        amazonS3Client.putObject(
                new PutObjectRequest(bucket, fullPathName, file).withCannedAcl(PublicRead));

        file.delete();

        return amazonS3Client.getUrl(bucket, fullPathName).toString();
    }

    private File convertToFile(MultipartFile multipartFile, String fullPathName) {

        File file = new File(fullPathName);

        try {
            file.createNewFile();

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(multipartFile.getBytes());
            }
        } catch (IOException e) {
            throw new InternalServerErrorException(IMAGE_UPLOAD_FAILED, e);
        }

        return file;
    }

    private String generateStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

    private String generateFullPath(String directoryName, String fileName) {
        return directoryName + "/" + fileName;
    }
}
