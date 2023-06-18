package com.plana.infli.repository.comment;

import java.util.List;

public interface CommentRepositoryCustom {

    void bulkDeleteByIds(List<Long> ids);

}
