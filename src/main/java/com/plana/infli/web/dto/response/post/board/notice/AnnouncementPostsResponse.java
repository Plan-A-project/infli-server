package com.plana.infli.web.dto.response.post.board.notice;

import com.plana.infli.web.dto.response.post.DefaultPost;
import com.plana.infli.web.dto.response.post.DefaultPostsResponse;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AnnouncementPostsResponse extends DefaultPostsResponse {


    @Builder
    public AnnouncementPostsResponse(int sizeRequest, int actualSize, int currentPage,
            List<? extends DefaultPost> posts) {
        super(sizeRequest, actualSize, currentPage, posts);
    }

    public static AnnouncementPostsResponse loadAnnouncementPostsResponse(
            List<AnnouncementPost> posts, int page) {

        return AnnouncementPostsResponse.builder()
                .sizeRequest(20)
                .actualSize(posts.size())
                .currentPage(page)
                .posts(posts)
                .build();
    }

}
