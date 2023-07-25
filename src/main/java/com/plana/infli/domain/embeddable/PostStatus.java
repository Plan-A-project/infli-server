package com.plana.infli.domain.embeddable;

import static lombok.AccessLevel.*;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Embeddable
public class PostStatus {

    private boolean isDeleted;

    private boolean isPublished;

    @Builder
    public PostStatus(boolean isDeleted, boolean isPublished) {
        this.isDeleted = isDeleted;
        this.isPublished = isPublished;
    }
}
