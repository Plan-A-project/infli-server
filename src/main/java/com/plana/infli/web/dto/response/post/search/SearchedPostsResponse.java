package com.plana.infli.web.dto.response.post.search;

import com.plana.infli.web.dto.request.post.view.PostQueryRequest;
import com.plana.infli.web.dto.response.post.DefaultPostsResponse;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SearchedPostsResponse extends DefaultPostsResponse {


    @Builder
    private SearchedPostsResponse(int sizeRequest, int actualSize,
            int currentPage, List<SearchedPost> posts) {
        super(sizeRequest, currentPage, actualSize, posts);
    }

    public static SearchedPostsResponse of(List<SearchedPost> posts,
            PostQueryRequest queryRequest) {

        return SearchedPostsResponse.builder()
                .sizeRequest(queryRequest.getSize())
                .currentPage(queryRequest.getPage())
                .posts(posts)
                .actualSize(posts.size())
                .build();
    }
}
