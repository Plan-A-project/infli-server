package com.plana.infli.domain;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.*;

import com.plana.infli.domain.editor.PostEditor;
import com.plana.infli.domain.embedded.post.Recruitment;
import com.plana.infli.domain.type.PostType;
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
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Nullable
    private String thumbnailUrl;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 조회수
    private int viewCount;

    // 해당 글에 댓글을 작성한 회원의 갯수
    private int commentMemberCount;

    private boolean isDeleted;

    @Embedded
    @Nullable
    private Recruitment recruitment;

    @OneToMany(mappedBy = "post")
    private List<PostLike> likes = new ArrayList<>();

    @Version
    private Long version;

    @Builder
    private Post(Board board, PostType postType, String title, String content,
            Member member, @Nullable Recruitment recruitment) {

        this.board = board;
        this.postType = postType;
        this.title = title;
        this.content = content;
        this.thumbnailUrl = null;
        this.member = member;
        this.viewCount = 0;
        this.commentMemberCount = 0;
        this.isDeleted = false;
        this.recruitment = recruitment;
    }

    public PostEditor.PostEditorBuilder toEditor() {
        return PostEditor.builder()
                .title(title)
                .content(content)
                .thumbnailUrl(thumbnailUrl)
                .recruitment(recruitment)
                .isDeleted(isDeleted);
    }

    public void edit(PostEditor postEditor) {
        this.title = postEditor.getTitle();
        this.content = postEditor.getContent();
        this.thumbnailUrl = postEditor.getThumbnailUrl();
        this.recruitment = postEditor.getRecruitment();
        this.isDeleted = postEditor.isDeleted();
        this.viewCount = postEditor.getViewCount();
    }

    public int increaseCommentMemberCount() {
        return ++commentMemberCount;
    }
}
