package com.plana.infli.web.dto.response.post.my;

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

    public static MyPostsResponse loadMyPostsResponse(int page, List<? extends DefaultPost> posts) {
        return MyPostsResponse.builder()
                .sizeRequest(20)
                .actualSize(posts.size())
                .currentPage(page)
                .posts(posts)
                .build();
    }
}
