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

        Post post = new Post(board, "제목입니다", "내용입니다", member);

        return postRepository.save(post);
    }
}
