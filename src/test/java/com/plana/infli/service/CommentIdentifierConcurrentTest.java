package com.plana.infli.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.Comment;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.Role;
import com.plana.infli.domain.University;
import com.plana.infli.factory.BoardFactory;
import com.plana.infli.factory.CommentFactory;
import com.plana.infli.factory.CommentLikeFactory;
import com.plana.infli.factory.MemberFactory;
import com.plana.infli.factory.PostFactory;
import com.plana.infli.factory.UniversityFactory;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.comment.CommentRepository;
import com.plana.infli.repository.commentlike.CommentLikeRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.request.comment.create.service.CreateCommentServiceRequest;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class CommentIdentifierConcurrentTest {


    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private CommentService commentService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private UniversityFactory universityFactory;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private CommentFactory commentFactory;

    @Autowired
    private PostFactory postFactory;

    @Autowired
    private BoardFactory boardFactory;

    @Autowired
    private CommentLikeFactory commentLikeFactory;

    @BeforeEach
    void tearDown() {
        commentRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        universityRepository.deleteAllInBatch();
    }

    @DisplayName("식별자 번호 동시성 테스트")
    @Test
    void identifierConcurrency() throws InterruptedException {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Board board = boardFactory.createAnonymousBoard(university);
        Post post = postFactory.createPost(createStudentMember("postMember", university), board);

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {

            int finalI = i;
            executorService.submit(() -> {

                try {
                    Member member = memberFactory.createStudentMember("" + finalI,
                            university);

                    commentService.createComment(CreateCommentServiceRequest.builder()
                            .content("댓글")
                            .parentCommentId(null)
                            .postId(post.getId())
                            .build(), member.getEmail());

                } catch (Exception e) {
                    System.out.println("에러 발생");
                    System.out.println(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Post findPost = postRepository.findPostById(post.getId()).get();
        assertThat(commentRepository.count()).isEqualTo(100);
        assertThat(findPost.getCommentMemberCount()).isEqualTo(100);
    }

    public Member createStudentMember(String nickname, University university) {
        Member member = Member.builder()
                .nickname(nickname)
                .name(nickname)
                .email(nickname + "@gmail.com")
                .password("1234")
                .university(university)
                .role(Role.STUDENT)
                .passwordEncoder(new BCryptPasswordEncoder())
                .build();

        return memberRepository.save(member);
    }
}
