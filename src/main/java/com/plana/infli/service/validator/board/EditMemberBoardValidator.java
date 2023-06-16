package com.plana.infli.service.validator.board;

import static com.plana.infli.exception.custom.NotFoundException.*;

import com.plana.infli.domain.Member;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.memberboard.MemberBoardRepository;
import com.plana.infli.service.MemberUtil;
import com.plana.infli.web.dto.request.board.EditMemberBoardRequest;
import com.plana.infli.web.dto.request.board.EditMemberBoardRequest.EditMemberBoardRequestBuilder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class EditMemberBoardValidator implements Validator {

    private final MemberBoardRepository memberBoardRepository;

    private final MemberUtil memberUtil;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(EditMemberBoardRequest.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EditMemberBoardRequest request = (EditMemberBoardRequest) target;

        Member member = memberUtil.getContextMember();

        List<Long> ids = request.getMemberBoardIds();

        checkWhetherMemberBoardsExist(member, ids);
    }

    private void checkWhetherMemberBoardsExist(Member member, List<Long> ids) {
        ids.forEach(i -> {
            if (memberBoardRepository.existsByIdAndMember(i, member)) {
                throw new NotFoundException(BOARD_NOT_FOUND);
            }
        });
    }
}
