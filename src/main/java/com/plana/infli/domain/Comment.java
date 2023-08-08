package com.plana.infli.domain;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import com.plana.infli.domain.editor.comment.CommentEditor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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
@SQLDelete(sql = "UPDATE comment SET is_deleted = true WHERE comment_id=?")
public class Comment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "comment_id")
	private Long id;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "post_id")
	private Post post;

    @Lob
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

    @OneToMany(mappedBy = "parentComment")
    private List<Comment> children = new ArrayList<>();

    @OneToMany(mappedBy = "comment")
    private List<CommentLike> commentLikes = new ArrayList<>();

    private boolean isDeleted = false;

    private boolean isEdited = false;

    // 글에 댓글을 작성한 회원들에 대한 식별자 번호
    // 회원이 어떤 글에 처음으로 댓글을 작성하는 경우 : 새로운 식별자 번호를 부여 받는다
    // 어떤 글에 대한 식별자를 부여 받은 회원이 동일한 글에 계속 댓글을 작성하더라도 첫 부여받은 식별자를 계속 부여받음
    // 글 작성자가 자신의 글에 댓글을 작성하는 경우 : 0번을 부여받는다
    // 글 작성자가 아닌 회원은 1번부터 부여 받으며,
    // 새로운 회원이 이 글에 댓글을 작성할 떄마다 1씩 증가된 번호를 부여받는다
    private int identifierNumber;


    @Builder
    private Comment(Post post, String content, Member member,
            Comment parentComment, int identifierNumber) {
        this.post = post;
        this.content = content;
        this.member = member;
        this.identifierNumber = identifierNumber;
        bindParentAndChildComment(parentComment);
    }

    public static Comment create(Post post, String content, Member member,
            Comment parentComment, Integer identifierNumber) {
        return Comment.builder()
                .post(post)
                .content(content)
                .member(member)
                .parentComment(parentComment)
                .identifierNumber(identifierNumber)
                .build();
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

    public CommentEditor.CommentEditorBuilder toEditor() {
        return CommentEditor.builder()
                .content(content)
                .isEdited(isEdited)
                .isDeleted(isDeleted);
    }

    /**
     * 여기서만 댓글 수정 가능
     */
    public void edit(CommentEditor commentEditor) {
        this.content = commentEditor.getContent();
        this.isEdited = commentEditor.isEdited();
        this.isDeleted = commentEditor.isDeleted();
    }

}
