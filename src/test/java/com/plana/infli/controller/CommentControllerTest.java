package com.plana.infli.controller;

import static org.assertj.core.api.Assertions.*;

import com.plana.infli.domain.Comment;
import com.plana.infli.repository.comment.CommentRepository;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public class CommentControllerTest {

    @Autowired
    private CommentRepository commentRepository;


    @DisplayName("")
    @Test
    void test() {
        //given

        //when

        //then
        List<Long> list = new ArrayList<>();

        assertThat(list).hasSize(2)
                .extracting("a", "b")
                .containsExactlyInAnyOrder(
                        tuple("1", "2", "1"),
                        tuple()
                );
    }
}
