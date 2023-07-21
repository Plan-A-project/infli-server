package com.plana.infli.domain;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.*;

import com.plana.infli.web.dto.request.post.GatherPostCreateRq;
import com.plana.infli.web.dto.request.post.PostCreateRq;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = PROTECTED)
@SQLDelete(sql = "UPDATE post SET is_deleted=true WHERE post_id=?")
@Entity
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "post_id")
    private Long id;

    private String title;

    private String main;

    @Enumerated(value = STRING)
    private PostType type;

    private boolean isDeleted = Boolean.FALSE;

    private boolean isPublished;

    private String enterprise;

    private LocalDate startDate;

    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 조회수
    private int viewCount = 0;

    // 좋아요 수
    private int likeCount = 0;

    // 해당 글에 댓글을 작성한 회원의 갯수
    private int commentMemberCount = 0;


    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Image> imageList = new ArrayList<>();

    @Builder
    public Post(String title, String main, PostType type, boolean isPublished,
                String enterprise, LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.main = main;
        this.type = type;
        this.isPublished = isPublished;
        this.enterprise = enterprise;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public static Post create(Member member, Board board) {
        Post post = Post.builder()
                .build();

        post.setMember(member);
        post.setBoard(board);
        return post;
    }

    public void publish(PostCreateRq request) {
        this.title = request.getTitle();
        this.main = request.getMain();
        this.isPublished = true;
        if (type.equals(PostType.GATHER)) {
            publish((GatherPostCreateRq) request);
        }
    }

    private void publish(GatherPostCreateRq request) {
        this.enterprise = request.getEnterprise();
        this.startDate = request.getStartDate();
        this.endDate = request.getEndDate();
    }

    public int increaseCount() {
        commentMemberCount++;
        return commentMemberCount;
    }

    public int plusViewCount() {
        viewCount++;
        return viewCount;
    }

    public int plusLikeCount() {
        likeCount++;
        return likeCount;
    }

    public int minusLikeCount() {
        likeCount--;
        return likeCount;
    }
}
