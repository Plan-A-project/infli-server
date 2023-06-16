package com.plana.infli.service.validator.board;

import static com.plana.infli.exception.custom.ConflictException.*;
import static com.plana.infli.exception.custom.NotFoundException.*;

import com.plana.infli.exception.custom.ConflictException;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.request.board.CreateBoardRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@RequiredArgsConstructor
@Component
public class CreateBoardValidator implements Validator {

    private final BoardRepository boardRepository;

    private final UniversityRepository universityRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(CreateBoardRequest.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CreateBoardRequest request = (CreateBoardRequest) target;

        if (anyNullExists(request)) {
            return;
        }

        // 해당 번호의 대학이 없는 경우
        if (universityRepository.existsById(request.getUniversityId()) == false) {
            throw new NotFoundException(UNIVERSITY_NOT_FOUND);
        }


        // 해당 이름의 게시판이 이미 존재하는 경우
        if (boardRepository.existsByBoardName(request.getBoardName())) {
            throw new ConflictException(DUPLICATED_BOARDNAME);
        }
    }

    private boolean anyNullExists(CreateBoardRequest request) {
        return request.getUniversityId() == null || request.getBoardName() == null
                || request.getIsAnonymous() == null;
    }
}
