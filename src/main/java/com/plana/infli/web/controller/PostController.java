package com.plana.infli.web.controller;

import static org.springframework.http.HttpStatus.*;

import com.plana.infli.service.PostService;
import com.plana.infli.web.dto.request.post.view.board.LoadPostsByBoardRequest;
import com.plana.infli.web.dto.request.post.create.CreatePostRequest;
import com.plana.infli.web.dto.request.post.edit.EditPostRequest;
import com.plana.infli.web.dto.request.post.view.search.SearchPostsByKeywordRequest;
import com.plana.infli.web.dto.response.post.board.BoardPostsResponse;
import com.plana.infli.web.dto.response.post.my.MyPostsResponse;
import com.plana.infli.web.dto.response.post.search.SearchedPostsResponse;
import com.plana.infli.web.dto.response.post.single.SinglePostResponse;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class PostController {

    private final PostService postService;

    @GetMapping("/posts/policy")
    @Operation(summary = "글 작성 이용 규칙 동의 여부 확인")
    public boolean checkMemberAgreedOnWritePolicy(
            @AuthenticatedPrincipal String email) {
        return postService.checkMemberAgreedOnWritePolicy(email);
    }

    @PostMapping("/posts/policy")
    @Operation(summary = "글 작성 이용 규칙 동의함 요청")
    public void agreedOnWritePolicy(@AuthenticatedPrincipal String email) {
        postService.confirmWritePolicyAgreement(email);
    }

    @ResponseStatus(CREATED)
    @PostMapping("/posts")
    @Operation(summary = "글 단건 생성")
    public Long write(@Validated @RequestBody CreatePostRequest request,
            @AuthenticatedPrincipal String email) {
        return postService.createPost(request.toServiceRequest(email));
    }

    @PatchMapping("/posts")
    @Operation(summary = "내가 작성한 글 단건 수정")
    public void edit(@Validated @RequestBody EditPostRequest request,
            @AuthenticatedPrincipal String email) {
        postService.editPost(request.toServiceRequest(email));
    }

    @DeleteMapping("/posts/{postId}")
    @Operation(summary = "내가 작성한 글 단건 삭제")
    public void delete(@PathVariable Long postId,
            @AuthenticatedPrincipal String email) {
        postService.deletePost(postId, email);
    }

    @GetMapping("/posts/{postId}")
    @Operation(summary = "글 단건 조회")
    public SinglePostResponse loadSinglePost(@PathVariable Long postId,
            @AuthenticatedPrincipal String email) {

        return postService.loadSinglePost(postId, email);
    }

    @GetMapping("/members/posts")
    @Operation(summary = "내가 작성한 글 목록 조회")
    public MyPostsResponse loadMyPosts(@RequestParam(defaultValue = "1") String page,
            @AuthenticatedPrincipal String email) {
        return postService.loadMyPosts(email, page);
    }

    @GetMapping("/posts/boards/{boardId}")
    @Operation(summary = "특정 게시판에 작성된 글 목록 조회")
    public BoardPostsResponse loadsPostsByBoard(@PathVariable Long boardId,
            @Validated LoadPostsByBoardRequest request, @AuthenticatedPrincipal String email) {

        return postService.loadPostsByBoard(request.toServiceRequest(boardId, email));
    }

    @GetMapping("/posts/search")
    @Operation(summary = "글 키워드로 검색")
    public SearchedPostsResponse searchPostsByKeyword(SearchPostsByKeywordRequest request,
            @AuthenticatedPrincipal String email) {
        return postService.searchPostsByKeyword(request.toServiceRequest(email));
    }

}
