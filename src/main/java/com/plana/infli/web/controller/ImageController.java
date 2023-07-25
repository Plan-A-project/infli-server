package com.plana.infli.web.controller;

import com.plana.infli.service.ImageService;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ImageController {

    private final ImageService imageService;

//    @PostMapping("/post/{postId}/image")
//    public ResponseEntity<List<ImageCreateRs>> createImage(@PathVariable Long postId,
//            MultipartFile multipartFile) throws IOException {
//
//        return imageService.createImage(postId, files, "post");
//    }

    @PostMapping("/posts/{postId}/images")
    public List<String> uploadImage(@PathVariable Long postId,
            List<MultipartFile> multipartFiles, @AuthenticatedPrincipal String email) {

        return imageService.uploadImagesToS3(postId, multipartFiles, email);
    }

//    @PostMapping("/upload")
//    @Operation(summary = "테스트용")
//    public String upload(@RequestParam("file") MultipartFile multipartFile) throws IOException {
//        return s3Uploader.upload(multipartFile, "static");
//    }
}
