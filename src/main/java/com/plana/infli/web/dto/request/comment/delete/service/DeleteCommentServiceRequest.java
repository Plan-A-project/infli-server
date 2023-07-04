package com.plana.infli.web.dto.request.comment.delete.service;

import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class DeleteCommentServiceRequest {

    List<Long> ids;

    @Builder
    public DeleteCommentServiceRequest(List<Long> ids) {
        this.ids = ids;
    }
}
