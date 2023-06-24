package com.plana.infli.service;

import static org.junit.jupiter.api.Assertions.*;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.repository.comment.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CommentServiceTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentService commentService;




    private Comment createComment(Post post, String content, Member member, Comment parentComment) {
        return Comment.builder()
                .post(post)
                .content(content)
                .member(member)
                .parentComment(parentComment).build();
    }

}