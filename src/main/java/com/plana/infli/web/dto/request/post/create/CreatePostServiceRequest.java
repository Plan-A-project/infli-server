package com.plana.infli.web.dto.request.post.create;

import static com.plana.infli.domain.embeddable.Recruitment.*;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.PostType;
import com.plana.infli.domain.embeddable.Recruitment;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreatePostServiceRequest {

    private final String email;

    private final Long boardId;

    private final PostType postType;

    private final String title;

    private final String content;

    @Nullable
    private final CreateRecruitmentServiceRequest recruitment;

    @Builder
    public CreatePostServiceRequest(String email, Long boardId, PostType postType, String title,
            String content, CreateRecruitmentServiceRequest recruitment) {
        this.email = email;
        this.boardId = boardId;
        this.postType = postType;
        this.title = title;
        this.content = content;
        this.recruitment = recruitment;
    }

    @Getter
    public static class CreateRecruitmentServiceRequest {

        private final String companyName;

        private final LocalDateTime startDate;

        private final LocalDateTime endDate;

        @Builder
        public CreateRecruitmentServiceRequest(String companyName, LocalDateTime startDate,
                LocalDateTime endDate) {
            this.companyName = companyName;
            this.startDate = startDate;
            this.endDate = endDate;
        }


        public static Recruitment createRecruitment(CreateRecruitmentServiceRequest request) {
            return create(request.companyName, request.startDate, request.endDate);
        }
    }


    public static Post of(Member member, Board board, CreatePostServiceRequest request,
            @Nullable Recruitment recruitment) {

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
