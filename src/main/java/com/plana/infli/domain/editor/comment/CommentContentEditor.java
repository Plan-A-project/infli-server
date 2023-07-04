package com.plana.infli.domain.editor.comment;

import com.plana.infli.domain.Comment;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CommentContentEditor {

    private final String content;

    @Builder
    public CommentContentEditor(String content) {
        this.content = content;
    }

    public static Comment editComment(Comment comment, String newContent) {
        return comment.edit(comment.toEditor()
                .content(newContent)
                .build());
    }
}
