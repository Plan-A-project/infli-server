package com.plana.infli.web.dto.request.comment;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Getter
@NoArgsConstructor
public class CreateCommentRequest {

    @NotNull(message = "글 번호가 입력되지 않았습니다")
    private Long postId;

    @Nullable
    private Long parentCommentId;

    @NotBlank(message = "내용을 입력해주세요")
    @Size(max = 500, message = "댓글은 500자 이하로 입력해주세요")
    private String content;

    @Builder
    public CreateCommentRequest(Long postId, @Nullable Long parentCommentId, String content) {
        this.postId = postId;
        this.parentCommentId = parentCommentId;
        this.content = content;
    }

    public static Comment createComment(Post post, Member member, Comment parentComment,
            String content) {

        return Comment.builder()
                .post(post)
                .member(member)
                .parentComment(parentComment)
                .content(content).build();
    }
}
