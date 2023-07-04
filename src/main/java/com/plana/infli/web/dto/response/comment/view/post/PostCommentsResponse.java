package com.plana.infli.web.dto.response.comment.view.post;

import com.plana.infli.web.dto.response.comment.view.BestCommentResponse;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;

@Getter
public class PostCommentsResponse {

    // 글 ID 번호
    private final Long postId;

    // 익명게시판에 작성된 댓글인지 여부
    // True -> 익명글임. 댓글 작성자의 닉네임 대신 식별자 번호가 조회됨
    // False -> 익명글 아님. 식별자 번호 대신 댓글 작성자의 닉네임이 조회됨
    private final boolean isAnonymousBoard;

    // 한 페이지당 조회되길 희망하는 댓글 갯수
    // 기본값 : 100개씩 조회
    private final int sizeRequest;

    // 현재 페이지에 실제로 조회된 댓글의 갯수
    // Ex) 어떤 글에 총 180개의 댓글이 달린 경우
    // 1페이지의 실제 조회된 댓글 : 100개
    // 2페이지의 실제 조회된 댓글 : 80개
    private final int actualSize;

    // 현재 페이지
    private final int currentPage;

    // 해당 댓글들을 관리자가 조회 요청했는지 여부
    // True : 조회 요청자가 관리자임
    // False : 일반 회원임
    private final boolean isAdmin;

    // 조회된 댓글 목록
    private final List<PostComment> comments ;

    @Builder
    public PostCommentsResponse(Long postId, boolean isAnonymousBoard,
            int sizeRequest, int actualSize, int currentPage,
            boolean isAdmin, List<PostComment> comments) {

        this.postId = postId;
        this.isAnonymousBoard = isAnonymousBoard;
        this.sizeRequest = sizeRequest;
        this.actualSize = actualSize;
        this.currentPage = currentPage;
        this.isAdmin = isAdmin;
        this.comments = comments;
    }

    public static PostCommentsResponse loadPostCommentsResponse(
            Long postId, boolean isAnonymousPost, PageRequest pageRequest,
            boolean isAdmin, List<PostComment> comments) {

        return PostCommentsResponse.builder()
                .postId(postId)
                .isAnonymousBoard(isAnonymousPost)
                .sizeRequest(pageRequest.getPageSize())
                .actualSize(comments != null ? comments.size() : 0)
                .currentPage(pageRequest.getPageNumber())
                .isAdmin(isAdmin)
                .comments(comments != null ? comments : new ArrayList<>())
                .build();
    }
}
