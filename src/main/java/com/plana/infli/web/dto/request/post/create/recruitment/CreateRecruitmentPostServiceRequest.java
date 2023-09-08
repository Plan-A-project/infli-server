package com.plana.infli.web.dto.request.post.create.recruitment;

import static com.plana.infli.domain.type.PostType.*;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.type.PostType;
import com.plana.infli.domain.embedded.post.Recruitment;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateRecruitmentPostServiceRequest {

    //TODO
    private final PostType postType = RECRUITMENT;

    private final String username;

    private final Long boardId;

    private final String title;

    private final String content;

    private final String recruitmentCompanyName;

    private final LocalDateTime recruitmentStartDate;

    private final LocalDateTime recruitmentEndDate;


    @Builder
    public CreateRecruitmentPostServiceRequest(String username, Long boardId, String title,
            String content, String recruitmentCompanyName, LocalDateTime recruitmentStartDate,
            LocalDateTime recruitmentEndDate) {

        this.username = username;
        this.boardId = boardId;
        this.title = title;
        this.content = content;
        this.recruitmentCompanyName = recruitmentCompanyName;
        this.recruitmentStartDate = recruitmentStartDate;
        this.recruitmentEndDate = recruitmentEndDate;
    }


    public Post toEntity(Member member, Board board, Recruitment recruitment) {
        return Post.builder()
                .board(board)
                .member(member)
                .postType(postType)
                .title(title)
                .content(content)
                .recruitment(recruitment)
                .build();
    }
}
