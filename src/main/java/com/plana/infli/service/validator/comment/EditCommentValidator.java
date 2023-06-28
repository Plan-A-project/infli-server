package com.plana.infli.service.validator.comment;

import static com.plana.infli.exception.custom.NotFoundException.*;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.Member;
import com.plana.infli.exception.custom.AuthorizationFailedException;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.comment.CommentRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.service.MemberUtil;
import com.plana.infli.web.dto.request.comment.EditCommentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EditCommentValidator implements Validator {

    private final CommentRepository commentRepository;

    private final MemberUtil memberUtil;

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

        Member member = memberUtil.getContextMember();

        validateComment(comment, member);
    }

    private void validateComment(Comment comment, Member member) {

        if (comment == null || comment.isEnabled() == false) {
            throw new NotFoundException(COMMENT_NOT_FOUND);
        }

        if (comment.getPost().isDeleted()) {
            throw new NotFoundException(POST_NOT_FOUND);
        }

        if (comment.getMember().equals(member) == false) {
            throw new AuthorizationFailedException();
        }
    }

}
