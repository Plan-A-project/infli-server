package com.plana.infli.web.dto.request.post.create.recruitment;

import static com.plana.infli.domain.PostType.*;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.PostType;
import com.plana.infli.domain.embedded.post.Recruitment;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateRecruitmentPostServiceRequest {

    private final PostType postType = RECRUITMENT;

    private final String email;

    private final Long boardId;

    private final String title;

    private final String content;

    private final String recruitmentCompanyName;

    private final LocalDateTime recruitmentStartDate;

    private final LocalDateTime recruitmentEndDate;


    @Builder
    public CreateRecruitmentPostServiceRequest(String email, Long boardId, String title,
            String content,
            String recruitmentCompanyName, LocalDateTime recruitmentStartDate,
            LocalDateTime recruitmentEndDate) {
        this.email = email;
        this.boardId = boardId;
        this.title = title;
        this.content = content;
        this.recruitmentCompanyName = recruitmentCompanyName;
        this.recruitmentStartDate = recruitmentStartDate;
        this.recruitmentEndDate = recruitmentEndDate;
    }


    public static Post toEntity(Member member, Board board, CreateRecruitmentPostServiceRequest request,
            Recruitment recruitment) {

        return Post.builder()
                .board(board)
                .member(member)
                .postType(request.getPostType())
                .title(request.getTitle())
                .content(request.getContent())
                .recruitment(recruitment)
                .build();
    }

}
