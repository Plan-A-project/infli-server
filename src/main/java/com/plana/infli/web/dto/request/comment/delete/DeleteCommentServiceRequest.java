package com.plana.infli.web.dto.request.comment.delete;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class DeleteCommentServiceRequest {

    private final String email;

    private final List<Long> ids;

    @Builder
    public DeleteCommentServiceRequest(String email, List<Long> ids) {
        this.email = email;
        this.ids = ids;
    }
}
