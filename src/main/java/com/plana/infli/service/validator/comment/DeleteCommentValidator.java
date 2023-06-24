package com.plana.infli.service.validator.comment;

import static com.plana.infli.exception.custom.BadRequestException.INVALID_REQUIRED_PARAM;
import static com.plana.infli.exception.custom.NotFoundException.*;
import static com.plana.infli.service.MemberUtil.isAdmin;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.Member;
import com.plana.infli.exception.custom.AuthorizationFailedException;
import com.plana.infli.exception.custom.BadRequestException;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.comment.CommentRepository;
import com.plana.infli.service.MemberUtil;
import com.plana.infli.web.dto.request.comment.DeleteCommentRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeleteCommentValidator implements Validator {

    private final CommentRepository commentRepository;

    private final MemberUtil memberUtil;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(DeleteCommentRequest.class);
    }

    @Override
    public void validate(Object target, Errors errors) {

        List<Long> ids = ((DeleteCommentRequest) target).getIds();

        if (ids.isEmpty()) {
            throw new BadRequestException(INVALID_REQUIRED_PARAM);
        }

        Member member = memberUtil.getContextMember();

        ids.forEach(i -> validateComment(commentRepository.findWithMemberById(i), member));
    }

    private void validateComment(Comment comment, Member member) {

        if (comment == null || comment.isEnabled() == false) {
            throw new NotFoundException(COMMENT_NOT_FOUND);
        }

        if (isAdmin()) {
            return;
        }

        if (comment.getMember().equals(member) == false) {
            throw new AuthorizationFailedException();
        }
    }
}
