package com.plana.infli.service.validator.board;

import static com.plana.infli.exception.custom.NotFoundException.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.service.MemberUtil;
import com.plana.infli.web.dto.request.board.CreateMemberBoardRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@RequiredArgsConstructor
@Component
public class CreateMemberBoardValidator implements Validator {

    private final BoardRepository boardRepository;

    private final UniversityRepository universityRepository;

    private final MemberUtil memberUtil;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(CreateMemberBoardRequest.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CreateMemberBoardRequest request = (CreateMemberBoardRequest) target;

        Member member = memberUtil.getContextMember();

        University university = universityRepository.findByMember(member);

        checkWhetherBoardsExist(request, university);
    }

    private void checkWhetherBoardsExist(CreateMemberBoardRequest request, University university) {

        List<Long> ids = request.getIds();

        ids.forEach(i -> {
            if (boardRepository.existsByIdAndUniversity(i, university) == false) {
                throw new NotFoundException(BOARD_NOT_FOUND);
            }
        });
    }
}
