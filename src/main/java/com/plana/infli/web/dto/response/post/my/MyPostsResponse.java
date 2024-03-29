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
    public MyPostsResponse(int sizeRequest, int currentPage, int actualSize,
            List<MyPost> posts) {
        super(sizeRequest, currentPage, actualSize, posts);
    }

    public static MyPostsResponse of(PostQueryRequest request,
            List<MyPost> posts) {

        return MyPostsResponse.builder()
                .sizeRequest(request.getSize())
                .currentPage(request.getPage())
                .actualSize(posts.size())
                .posts(posts)
                .build();
    }
}
