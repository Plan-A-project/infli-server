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

    public static void editComment(Comment comment, String newContent) {
        comment.edit(comment.toEditor()
                .content(newContent)
                .build());
    }
}
