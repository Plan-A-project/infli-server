package com.plana.infli.utils;

import static com.plana.infli.exception.custom.InternalServerErrorException.*;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.plana.infli.exception.custom.InternalServerErrorException;
import java.io.IOException;
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

        String fullPath = generateFullPath(directoryPath, storeFileName);

        File file = new File(fullPath);

        writeIntoFile(multipartFile, file);

        PutObjectRequest request = generatePutObjectRequest(fullPath, file);

        amazonS3Client.putObject(request);

        file.delete();

        return amazonS3Client.getUrl(bucket, fullPath).toString();
    }

    private PutObjectRequest generatePutObjectRequest(String fullPath, File file) {
        PutObjectRequest request = new PutObjectRequest(bucket, fullPath, file);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.length());
        request.setMetadata(metadata);
        return request;
    }

    private void writeIntoFile(MultipartFile multipartFile, File file) {
        try {
            if (file.createNewFile()) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(multipartFile.getBytes());
                }
            }
        } catch (IOException e) {
            throw new InternalServerErrorException(IMAGE_UPLOAD_FAILED, e);
        }
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
        return directoryName + fileName;
    }

//    private String upload(File uploadFile, String dirName) {
//        String fileName = dirName + "/" + getUploadFileName(uploadFile.getName());
//        String uploadImageUrl = putS3(uploadFile, fileName);
//        removeNewFile(uploadFile);
//        return uploadImageUrl;
//    }
//
//    private String getUploadFileName(String fileFullName) {
//        return fileFullName.substring(0, fileFullName.lastIndexOf("."))
//                + "_" + System.currentTimeMillis()
//                + fileFullName.substring(fileFullName.lastIndexOf("."));
//    }
//
//    private String putS3(File uploadFile, String fileName) {
//        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
//    }
//
//    private void removeNewFile(File targetFile) {
//        if (targetFile.delete()) {
//            log.info("파일이 삭제되었습니다.");
//        } else {
//            log.info("파일이 삭제되지 못했습니다.");
//        }
//    }

}
