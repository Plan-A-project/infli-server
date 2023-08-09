package com.plana.infli.web.dto.request.post.create.normal;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.PostType;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateNormalPostServiceRequest {

    private final String email;

    private final Long boardId;

    private final String title;

    private final String content;

    private final PostType postType;

    @Builder
    public CreateNormalPostServiceRequest(String email, Long boardId,
            String title, String content, PostType postType) {

        this.email = email;
        this.boardId = boardId;
        this.title = title;
        this.content = content;
        this.postType = postType;
    }


    public static Post toEntity(Member member, Board board,
            CreateNormalPostServiceRequest request) {

        return Post.builder()
                .board(board)
                .member(member)
                .postType(request.getPostType())
                .title(request.getTitle())
                .content(request.getContent())
                .build();
    }
}
