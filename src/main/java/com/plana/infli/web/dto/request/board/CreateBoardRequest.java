package com.plana.infli.web.dto.request.board;

import static lombok.AccessLevel.*;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.University;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = PROTECTED)
public class CreateBoardRequest {

    @NotBlank(message = "대학교 번호는 필수입니다")
    private Long universityId;

    @NotBlank(message = "게시판 이름을 입력해주세요")
    private String boardName;

    @NotBlank(message = "익명 여부를 선택해주세요")
    private Boolean isAnonymous;

    @Builder
    public CreateBoardRequest(Long universityId, String boardName, Boolean isAnonymous) {
        this.universityId = universityId;
        this.boardName = boardName;
        this.isAnonymous = isAnonymous;
    }
}
