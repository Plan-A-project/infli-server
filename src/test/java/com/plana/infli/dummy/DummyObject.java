package com.plana.infli.dummy;

import static com.plana.infli.domain.BoardType.*;
import static com.plana.infli.domain.Role.*;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.BoardType;
import com.plana.infli.domain.Comment;
import com.plana.infli.domain.CommentLike;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.Role;
import com.plana.infli.domain.University;
import java.util.UUID;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public abstract class DummyObject {

    protected static University newUniversity() {
        return University.builder()
                .name(UUID.randomUUID().toString())
                .build();
    }

    protected static Board newAnonymousBoard(University university) {
        return Board.create(ANONYMOUS, university);
    }

    protected static Board newClubBoard(University university) {
        return Board.create(CLUB, university);
    }

    protected static Board newCampusLifeBoard(University university) {
        return Board.create(CAMPUS_LIFE, university);
    }

    protected static Board newEmploymentBoard(University university) {
        return Board.create(EMPLOYMENT, university);
    }

    protected static Board newActivityBoard(University university) {
        return Board.create(ACTIVITY, university);
    }

    protected static Post newPost(Member member, Board board) {
        return Post.create(member, board);
    }

    protected static Member newStudentMember(University university) {
        return Member.builder()
                .email(UUID.randomUUID().toString())
                .passwordEncoder(new BCryptPasswordEncoder())
                .password(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .nickname(UUID.randomUUID().toString())
                .role(STUDENT)
                .university(university)
                .build();
    }

    protected static Member newUncertifiedMember(University university) {
        return Member.builder()
                .email(UUID.randomUUID().toString())
                .passwordEncoder(new BCryptPasswordEncoder())
                .password(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .nickname(UUID.randomUUID().toString())
                .role(UNCERTIFIED)
                .university(university)
                .build();
    }

    protected static Member newAdminMember(University university) {
        return Member.builder()
                .email(UUID.randomUUID().toString())
                .passwordEncoder(new BCryptPasswordEncoder())
                .password(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .nickname(UUID.randomUUID().toString())
                .role(ADMIN)
                .university(university)
                .build();
    }

    protected static Comment newParentComment(Post post, Member member, Integer identifierNumber) {
        return Comment.create(post, UUID.randomUUID().toString(), member, null, identifierNumber);
    }


    //TODO identifier
    protected static Comment newChildComment(Post post, Member member, Comment parentComment,
            Integer identifierNumber) {

        return Comment.create(post, UUID.randomUUID().toString(), member, parentComment,
                identifierNumber);
    }


    protected static CommentLike newCommentLike(Comment comment, Member member) {
        return CommentLike.create(comment, member);
    }

}
