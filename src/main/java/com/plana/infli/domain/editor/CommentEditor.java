package com.plana.infli.domain.editor;

import com.plana.infli.domain.Comment;
import com.plana.infli.web.dto.request.comment.EditCommentRequest;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CommentEditor {

    private final String content;

    @Builder
    public CommentEditor(String content) {
        this.content = content;
    }

    public static void editComment(Comment comment, EditCommentRequest request) {
        comment.edit(comment.toEditor()
                .content(request.getContent())
                .build());
    }
}
