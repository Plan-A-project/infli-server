package com.plana.infli.web.dto.response.comment.view.post;

import static com.plana.infli.domain.Board.isAnonymous;

import com.plana.infli.domain.Member;
import com.plana.infli.web.dto.request.comment.view.CommentQueryRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostCommentsResponse {

    // 익명게시판에 작성된 댓글인지 여부
    // True -> 익명글임. 댓글 작성자의 닉네임 대신 식별자 번호가 조회됨
    // False -> 익명글 아님. 식별자 번호 대신 댓글 작성자의 닉네임이 조회됨
    private final boolean isAnonymousBoard;

    // 한 페이지당 조회되길 희망하는 댓글 갯수
    // 기본값 : 100개씩 조회
    private final int sizeRequest;

    // 현재 페이지
    private final int currentPage;

    // 해당 댓글들을 관리자가 조회 요청했는지 여부
    // True : 조회 요청자가 관리자임
    // False : 일반 회원임
    private final boolean isAdmin;

    // 조회된 댓글 목록
    private final List<PostComment> comments;

    @Builder
    public PostCommentsResponse(boolean isAnonymousBoard,
            int sizeRequest, int currentPage,
            boolean isAdmin, List<PostComment> comments) {

        this.isAnonymousBoard = isAnonymousBoard;
        this.sizeRequest = sizeRequest;
        this.currentPage = currentPage;
        this.isAdmin = isAdmin;
        this.comments = comments;
    }

    public static PostCommentsResponse of(
            List<PostComment> comments, CommentQueryRequest request) {

        return PostCommentsResponse.builder()
                .isAnonymousBoard(isAnonymous(request.getPost().getBoard()))
                .sizeRequest(request.getSize())
                .currentPage(request.getPage())
                .isAdmin(Member.isAdmin(request.getMember()))
                .comments(comments != null ? comments : new ArrayList<>())
                .build();
    }
}
