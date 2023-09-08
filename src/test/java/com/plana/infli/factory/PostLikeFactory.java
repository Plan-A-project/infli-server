package com.plana.infli.factory;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.PostLike;
import com.plana.infli.repository.postlike.PostLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostLikeFactory {

    @Autowired
    private PostLikeRepository postLikeRepository;

    public PostLike createPostLike(Member member, Post post) {
        return postLikeRepository.save(PostLike.create(post, member));
    }
}
