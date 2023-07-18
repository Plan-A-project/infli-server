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


    public Post createPost(Member member, Board board) {

        Post post = Post.builder()
                .title("제목")
                .main("내용").build();

        post.setBoard(board);
        post.setMember(member);

        return postRepository.save(post);
    }
}
