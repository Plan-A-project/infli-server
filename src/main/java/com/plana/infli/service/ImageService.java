package com.plana.infli.service;

import static com.plana.infli.exception.custom.BadRequestException.IMAGE_NOT_PROVIDED;
import static com.plana.infli.exception.custom.NotFoundException.*;

import com.plana.infli.domain.Member;
import com.plana.infli.exception.custom.AuthorizationFailedException;
import com.plana.infli.exception.custom.BadRequestException;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.member.MemberRepository;

import java.util.ArrayList;
import java.util.List;
import net.coobird.thumbnailator.Thumbnailator;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.plana.infli.domain.Post;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.utils.S3Uploader;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ImageService {

	private final PostRepository postRepository;

	private final MemberRepository memberRepository;

	private final S3Uploader s3Uploader;

//	@Transactional
//	public List<String> createImage(Post post, List<MultipartFile> files, String dirName) {
//
//		List<Image> imageList = imageRepository.findByPostIdOrderByPageDesc(postId);
//		int index = 1;
//		if (!imageList.isEmpty()) {
//			index = imageList.get(0).getPage() + 1;
//		}
//
//		List<ImageCreateRs> saveImages = new ArrayList<>();
//		for (MultipartFile file : files) {
//			String path = dirName + "/post_" + post.getId();
//			String imageUrl = s3Uploader.upload(file, path);
//			Image image = Image.builder()
//					.imageUrl(imageUrl)
//					.isDeleted(false)
//					.page(index)
//					.build();
//			image.setPost(post);
//			imageRepository.save(image);
//			saveImages.add(new ImageCreateRs(image));
//			index++;
//		}
//		return ResponseEntity.ok().body(saveImages);
//	}

	@Transactional
	public List<String> uploadImagesToS3(Long postId, List<MultipartFile> multipartFiles, String email) {

		validateImages(multipartFiles);

		Member member = memberRepository.findActiveMemberBy(email)
				.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

		Post post = postRepository.findActivePostWithMemberBy(postId)
				.orElseThrow(() -> new NotFoundException(POST_NOT_FOUND));

		checkThisMemberIsPostWriter(member, post);

		String directoryPath = "post/post_" + post.getId();

		List<String> imagesUrl = new ArrayList<>();

		multipartFiles.forEach(multipartFile -> {
			String url = s3Uploader.upload(multipartFile, directoryPath);
			imagesUrl.add(url);
		});
//		File file = convertToFile(multipartFile);
//
//		String fileFullName = loadFileFullName(post.getId(), file.getName());
//
//		String uploadImageURL = uploadToS3(file, fileFullName);
//		file.delete();

		return imagesUrl;
	}

//	private String uploadToS3(File file, String fileFullName) {
//
//		amazonS3Client.putObject(
//				new PutObjectRequest(bucket, fileFullName, file).withCannedAcl(PublicRead));
//
//		return amazonS3Client.getUrl(bucket, fileFullName).toString();
//	}
//
//	private String loadFileFullName(Long postId, String imageName) {
//		String s3DirectoryPath = "post/post_" + postId;
//
//		return s3DirectoryPath + "/" + imageName.substring(0, imageName.lastIndexOf("."))
//				+ "_" + System.currentTimeMillis()
//				+ imageName.substring(imageName.lastIndexOf("."));
//	}
//
//
	private void checkThisMemberIsPostWriter(Member member, Post post) {
		if (post.getMember().equals(member) == false) {
			throw new AuthorizationFailedException();
		}
	}


	//TODO
	private  void validateImages(List<MultipartFile> files) {

		if (files.size() > 11) {
			throw new BadRequestException("");
		}

		if (files.isEmpty()) {
			throw new BadRequestException(IMAGE_NOT_PROVIDED);
		}

		files.forEach(file -> {
			if (file.isEmpty()) {
				throw new BadRequestException(IMAGE_NOT_PROVIDED);
			}
		});
	}
//
//	private File convertToFile(MultipartFile multipartFile) {
//		File file = new File(multipartFile.getOriginalFilename());
//
//		try {
//			if (file.createNewFile()) {
//				try (FileOutputStream outputStream = new FileOutputStream(file)) {
//					outputStream.write(multipartFile.getBytes());
//				}
//			}
//			return file;
//		} catch (IOException e) {
//			throw new InternalServerErrorException(IMAGE_UPLOAD_FAILED, e);
//		}
//	}
}
