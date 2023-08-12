package com.plana.infli.factory;

import static com.plana.infli.domain.type.BoardType.*;
import static com.plana.infli.domain.type.PostType.*;
import static com.plana.infli.domain.type.MemberRole.*;
import static com.plana.infli.domain.embedded.post.Recruitment.*;
import static com.plana.infli.exception.custom.BadRequestException.INVALID_BOARD_TYPE;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.type.BoardType;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.type.PostType;
import com.plana.infli.domain.type.MemberRole;
import com.plana.infli.domain.embedded.post.Recruitment;
import com.plana.infli.exception.custom.AuthorizationFailedException;
import com.plana.infli.exception.custom.BadRequestException;
import com.plana.infli.repository.post.PostRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostFactory {

    @Autowired
    private PostRepository postRepository;


    public Post createNormalPost(Member member, Board board) {

        Post post = of(member, board, NORMAL, null);

        return postRepository.save(post);
    }

    public Post createRecruitmentPost(Member member, Board board) {

        checkIfInvalidRecruitment(board.getBoardType());

        Recruitment recruitment = createRecruitment();

        Post post = of(member, board, RECRUITMENT, recruitment);

        return postRepository.save(post);
    }


    public Post createPost(Member member, Board board, PostType postType) {

        if (postType == RECRUITMENT) {
            return createRecruitmentPost(member, board);
        }

        if (postType == ANNOUNCEMENT) {
            return createAnnouncementPost(member, board);
        }

        return createNormalPost(member, board);
    }


    public Post createAnnouncementPost(Member member, Board board) {

        checkIfInvalidAnnouncement(member.getRole(), board.getBoardType());

        Post post = of(member, board, ANNOUNCEMENT, null);

        return postRepository.save(post);
    }

    private static Post of(Member member, Board board, PostType postType, Recruitment recruitment) {

        return Post.builder()
                .board(board)
                .postType(postType)
                .title("제목입니다")
                .content("내용입니다")
                .member(member)
                .recruitment(recruitment)
                .build();
    }

    private void checkIfInvalidRecruitment(BoardType boardType) {
        if (boardType != EMPLOYMENT && boardType != ACTIVITY) {
            throw new BadRequestException(INVALID_BOARD_TYPE);
        }
    }

    private void checkIfInvalidAnnouncement(MemberRole memberRole, BoardType boardType) {
        if (memberRole != ADMIN && memberRole != STUDENT_COUNCIL) {
            throw new AuthorizationFailedException();
        }

        if (boardType != CAMPUS_LIFE) {
            throw new BadRequestException(INVALID_BOARD_TYPE);
        }
    }

    private static Recruitment createRecruitment() {
        return create("카카오", LocalDateTime.of(2023, 8, 1, 0, 0),
                LocalDateTime.of(2023, 8, 2, 0, 0));
    }


}
