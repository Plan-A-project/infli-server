package com.plana.infli.domain.editor.comment;

import com.plana.infli.domain.Comment;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CommentEditor {

    private final String content;

    private final boolean isEdited;

    private final boolean isDeleted;


    @Builder
    public CommentEditor(String content, boolean isEdited, boolean isDeleted) {
        this.content = content;
        this.isEdited = isEdited;
        this.isDeleted = isDeleted;
    }


    public static void editContent(Comment comment, String newContent) {
        comment.edit(comment.toEditor()
                .content(newContent)
                .isEdited(true)
                .build());
    }

    public static void delete(Comment comment) {
        comment.edit(comment.toEditor()
                .isDeleted(true)
                .build());
    }
}
