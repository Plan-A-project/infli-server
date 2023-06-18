package com.plana.infli.service.validator.comment;

import static com.plana.infli.exception.custom.NotFoundException.*;
import static com.plana.infli.service.MemberUtil.isMyInfo;

import com.plana.infli.domain.Comment;
import com.plana.infli.exception.custom.AuthorizationFailedException;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.comment.CommentRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.request.comment.EditCommentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class EditCommentValidator implements Validator {

    private final CommentRepository commentRepository;


    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(EditCommentRequest.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EditCommentRequest request = (EditCommentRequest) target;

        if (request.getCommentId() == null || request.getContent() == null) {
            return;
        }

        Comment comment = commentRepository.findWithMemberAndPostById(
                request.getCommentId());

        validateComment(comment);
    }

    private void validateComment(Comment comment) {

        if (comment == null || comment.isEnabled() == false) {
            throw new NotFoundException(COMMENT_NOT_FOUND);
        }

        //TODO
        // Authentication 객체 안에 어떤 값 넣을 것인지
        if (isMyInfo(comment.getMember().getNickname()) == false) {
            throw new AuthorizationFailedException();
        }

        if (comment.getPost().isDeleted()) {
            throw new NotFoundException(POST_NOT_FOUND);
        }

    }
}
