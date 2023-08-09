package com.plana.infli.domain.editor;

import static com.plana.infli.domain.embedded.comment.CommentStatus.*;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.embedded.comment.CommentStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CommentEditor {

    private final String content;

    private final CommentStatus status;

    @Builder
    public CommentEditor(String content, CommentStatus status) {
        this.content = content;
        this.status = status;
    }

    public static void editContent(Comment comment, String newContent) {
        comment.edit(comment.toEditor()
                .content(newContent)
                .status(ofEdited())
                .build());
    }

    public static void delete(Comment comment) {
        comment.edit(comment.toEditor()
                .status(ofDeleted())
                .build());
    }
}
