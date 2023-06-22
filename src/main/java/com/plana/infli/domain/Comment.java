package com.plana.infli.domain;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.plana.infli.domain.editor.CommentEditor;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@SQLDelete(sql = "UPDATE comment SET is_enabled = false WHERE comment_id=?")
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    private String content;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "rood_id")
    private Comment root;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parentComment;

    @OneToMany(mappedBy = "parent")
    private List<Comment> children = new ArrayList<>();

    @OneToMany(mappedBy = "comment", cascade = ALL, orphanRemoval = true)
    private List<CommentLike> likes = new ArrayList<>();

    private boolean isEnabled = true;

    private boolean isEdited = false;


    public Comment(Post post, String content, Member member, Comment parentComment) {
        this.post = post;
        this.content = content;
        this.member = member;
        bindParentAndChildComment(parentComment);
    }

    public static Comment create(Post post, String content, Member member, Comment parentComment) {
        return new Comment(post, content, member, parentComment);
    }

    private void bindParentAndChildComment(Comment parentComment) {
        if (parentComment == null) {
            this.root = this;
        } else {
            this.root = parentComment;
            this.parentComment = parentComment;
            parentComment.getChildren().add(this);
        }
    }

    /** 여기서만 댓글 수정 가능 */
    public void edit(CommentEditor commentEditor) {
        this.content = commentEditor.getContent();
        this.isEdited = true;
    }

    public CommentEditor.CommentEditorBuilder toEditor() {
        return CommentEditor.builder()
                .content(content);
    }

}
