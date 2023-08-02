package com.plana.infli.domain;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.*;

import com.plana.infli.domain.editor.post.PostEditor;
import com.plana.infli.domain.embeddable.Recruitment;
import com.plana.infli.service.PostService;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

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

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @Enumerated(value = STRING)
    private PostType postType;

    private String title;

    @Lob
    private String content;

    @Nullable
    private String thumbnailUrl;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 조회수
    private int viewCount = 0;

    // 해당 글에 댓글을 작성한 회원의 갯수
    private int commentMemberCount = 0;

    private boolean isDeleted = false;

    @Version
    private Long version;

    @Embedded
    @Nullable
    private Recruitment recruitment;

    @OneToMany(mappedBy = "post")
    private List<PostLike> likes = new ArrayList<>();

    @Builder
    private Post(Board board, PostType postType, Member member,
            String title, String content, @Nullable Recruitment recruitment) {
        this.board = board;
        this.postType = postType;
        this.member = member;
        this.title = title;
        this.content = content;
        this.recruitment = recruitment;
    }

    public PostEditor.PostEditorBuilder toEditor() {
        return PostEditor.builder()
                .title(title)
                .content(content)
                .thumbnailUrl(thumbnailUrl)
                .recruitment(recruitment);
    }

    public void edit(PostEditor postEditor) {
        this.title = postEditor.getTitle();
        this.content = postEditor.getContent();
        this.thumbnailUrl = postEditor.getThumbnailUrl();
        this.recruitment = postEditor.getRecruitment();
    }

    //TODO 이름 변경 필요
    public int increaseCount() {
        commentMemberCount++;
        return commentMemberCount;
    }

    public static void plusViewCount(Post post) {
        post.viewCount++;
    }

    public static void delete(Post post) {
        post.isDeleted = true;
    }
}
