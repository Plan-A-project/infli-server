package com.plana.infli.factory;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostFactory {

    @Autowired
    private PostRepository postRepository;


    /// TODO Post 클래스 생성자 확인 필요
    public Post createPost(Member member, Board board) {

        Post post = Post.builder()
                .member(member)
                .board(board)
                .title("제목입니다")
                .content("내용입니다")
                .build();

        return postRepository.save(post);
    }
}
