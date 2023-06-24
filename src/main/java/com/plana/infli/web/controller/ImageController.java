package com.plana.infli.web.controller;

import com.plana.infli.service.ImageService;
import com.plana.infli.utils.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class ImageController {

    private final ImageService imageService;
    private final S3Uploader s3Uploader;

    @PostMapping("/{postId}/image")
    public ResponseEntity createImage(@PathVariable Long postId, @RequestParam("file") List<MultipartFile> files) throws IOException {
        return imageService.createImage(postId, files, "post");
    }

    // 테스트용
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        return s3Uploader.upload(multipartFile, "static");
    }

}
