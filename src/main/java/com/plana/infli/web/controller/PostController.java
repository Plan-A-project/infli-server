package com.plana.infli.web.controller;

import static org.springframework.http.HttpStatus.*;

import com.plana.infli.service.PostService;
import com.plana.infli.web.dto.request.post.view.board.LoadPostsByBoardRequest;
import com.plana.infli.web.dto.request.post.initialize.PostInitializeRequest;
import com.plana.infli.web.dto.request.post.edit.EditPostRequest;
import com.plana.infli.web.dto.response.post.PostsByBoardResponse;
import com.plana.infli.web.dto.response.post.my.MyPostsResponse;
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
    public Long initializePost(@Validated @RequestBody PostInitializeRequest request,
            @AuthenticatedPrincipal String email) {
        return postService.createInitialPost(request.toServiceRequest(), email);
    }

    @PatchMapping("/posts")
    public void editPost(@Validated @RequestBody EditPostRequest request,
            @AuthenticatedPrincipal String email) {
        postService.edit(request.toServiceRequest(), email);
    }

    @DeleteMapping("/posts/{postId}")
    public void deletePost(@PathVariable Long postId,
            @AuthenticatedPrincipal String email) {
        postService.deletePost(postId, email);
    }

    @GetMapping("/posts/{postId}")
    @Operation(summary = "글 단건 조회")
    public SinglePostResponse findSinglePost(@PathVariable Long postId,
            @AuthenticatedPrincipal String email) {

        return postService.findSinglePost(postId, email);
    }

    @GetMapping("/members/posts")
    public MyPostsResponse findMyPosts(String page,
            @AuthenticatedPrincipal String email) {
        return postService.findMyPosts(email, page);
    }

    @GetMapping("/posts/boards/{boardId}")
    public PostsByBoardResponse loadsPostsByBoard(@PathVariable Long boardId,
            LoadPostsByBoardRequest request,
            @AuthenticatedPrincipal String email) {
        return postService.loadPostsByBoard(boardId, request.toServiceRequest(), email);
    }
}
