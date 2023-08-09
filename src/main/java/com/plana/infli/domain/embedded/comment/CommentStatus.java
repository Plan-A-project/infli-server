package com.plana.infli.domain.embedded.comment;

import static lombok.AccessLevel.*;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Embeddable
public class CommentStatus {

    private boolean isDeleted;

    private boolean isEdited;

    @Builder
    private CommentStatus(boolean isDeleted, boolean isEdited) {
        this.isDeleted = isDeleted;
        this.isEdited = isEdited;
    }

    public static CommentStatus of(boolean isDeleted, boolean isEdited) {
        return CommentStatus.builder()
                .isDeleted(isDeleted)
                .isEdited(isEdited)
                .build();
    }

    public static CommentStatus ofDefault() {
        return of(false, false);
    }

    public static CommentStatus ofEdited() {
        return of(false, true);
    }

    public static CommentStatus ofDeleted() {
        return of(true, true);
    }
}
