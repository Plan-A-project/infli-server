package com.plana.infli.web.dto.response.post.my;

import com.plana.infli.web.dto.request.post.view.PostQueryRequest;
import com.plana.infli.web.dto.response.post.DefaultPost;
import com.plana.infli.web.dto.response.post.DefaultPostsResponse;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MyPostsResponse extends DefaultPostsResponse {


    @Builder
    public MyPostsResponse(int sizeRequest, int actualSize, int currentPage,
            List<? extends DefaultPost> posts) {
        super(sizeRequest, actualSize, currentPage, posts);
    }

    public static MyPostsResponse loadMyPostsResponse(PostQueryRequest request,
            List<? extends DefaultPost> posts) {

        return MyPostsResponse.builder()
                .sizeRequest(request.getSize())
                .actualSize(posts.size())
                .currentPage(request.getPage())
                .posts(posts)
                .build();
    }
}
