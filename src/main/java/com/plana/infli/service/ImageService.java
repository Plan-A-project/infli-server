package com.plana.infli.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.plana.infli.domain.Image;
import com.plana.infli.domain.Post;
import com.plana.infli.repository.post.ImageRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.utils.S3Uploader;
import com.plana.infli.web.dto.request.image.ImageCreateRs;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ImageService {

	private final PostRepository postRepository;
	private final ImageRepository imageRepository;
	private final S3Uploader s3Uploader;

	@Transactional
	public ResponseEntity<List<ImageCreateRs>> createImage(Long postId, List<MultipartFile> files, String dirName) throws IOException {

		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + postId));

		List<Image> imageList = imageRepository.findByPostIdOrderByPageDesc(postId);
		int index = 1;
		if (!imageList.isEmpty()) {
			index = imageList.get(0).getPage() + 1;
		}

		List<ImageCreateRs> saveImages = new ArrayList<>();
		for (MultipartFile file : files) {
			String path = dirName + "/post_" + postId;
			String imageUrl = s3Uploader.upload(file, path);
			Image image = Image.builder()
				.imageUrl(imageUrl)
				.isDeleted(false)
				.page(index)
				.build();
			image.setPost(post);
			imageRepository.save(image);
			saveImages.add(new ImageCreateRs(image));
			index++;
		}
		return ResponseEntity.ok().body(saveImages);
	}

}
