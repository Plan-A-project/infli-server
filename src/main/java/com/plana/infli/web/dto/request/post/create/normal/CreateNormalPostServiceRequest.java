package com.plana.infli.web.dto.request.post.create.normal;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.type.PostType;
import lombok.Builder;
import lombok.Getter;

public record CreateNormalPostServiceRequest(String username, Long boardId, String title,
                                             String content, PostType postType) {

    @Builder
    public CreateNormalPostServiceRequest {

    }


    public static Post toEntity(Member member, Board board,
            CreateNormalPostServiceRequest request) {

        return Post.builder()
                .board(board)
                .member(member)
                .postType(request.postType())
                .title(request.title())
                .content(request.content())
                .build();
    }
}
