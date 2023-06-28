package com.plana.infli.service.validator.comment;

import static com.plana.infli.exception.custom.BadRequestException.*;
import static com.plana.infli.exception.custom.NotFoundException.*;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.exception.custom.BadRequestException;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.comment.CommentRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.service.MemberUtil;
import com.plana.infli.web.dto.request.comment.CreateCommentRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreateCommentValidator implements Validator {

	private final PostRepository postRepository;

	private final CommentRepository commentRepository;

	private final UniversityRepository universityRepository;

	private final MemberUtil memberUtil;

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.isAssignableFrom(CreateCommentRequest.class);
	}

	@Override
	public void validate(Object target, Errors errors) {
		CreateCommentRequest request = (CreateCommentRequest)target;

		if (request.getPostId() == null || request.getContent() == null) {
			return;
		}

		Member member = memberUtil.getContextMember();

		Post post = postRepository.findPostById(request.getPostId());

		validatePost(post, member);

		if (request.getParentCommentId() == null) {
			return;
		}

		Comment parentComment = commentRepository.findWithPostById(request.getParentCommentId());
		validateParentComment(post, parentComment);

	}

	private void validatePost(Post post, Member member) {

		if (post == null || post.isDeleted()) {
			throw new NotFoundException(POST_NOT_FOUND);
		}

		if (universityRepository.isMemberAndPostInSameUniversity(member, post) == false) {
			throw new NotFoundException(POST_NOT_FOUND);
		}
	}

	private void validateParentComment(Post post, Comment parentComment) {

		if (parentComment == null || parentComment.isEnabled() == false) {
			throw new NotFoundException(COMMENT_NOT_FOUND);
		}

		if (parentComment.getPost().getId() != post.getId()) {
			throw new NotFoundException(COMMENT_NOT_FOUND);
		}

		if (parentComment.getParent() != null) {
			throw new BadRequestException(Child_Comments_NOT_ALLOWED);
		}
	}
}
