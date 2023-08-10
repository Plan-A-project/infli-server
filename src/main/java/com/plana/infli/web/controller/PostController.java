package com.plana.infli.web.controller;

import static com.plana.infli.web.dto.response.ApiResponse.*;

import com.plana.infli.domain.PostType;
import com.plana.infli.service.PostService;
import com.plana.infli.web.dto.request.post.create.recruitment.CreateRecruitmentPostRequest;
import com.plana.infli.web.dto.request.post.edit.recruitment.EditRecruitmentPostRequest;
import com.plana.infli.web.dto.request.post.view.board.LoadPostsByBoardRequest;
import com.plana.infli.web.dto.request.post.create.normal.CreateNormalPostRequest;
import com.plana.infli.web.dto.request.post.edit.normal.EditNormalPostRequest;
import com.plana.infli.web.dto.request.post.view.search.SearchPostsByKeywordRequest;
import com.plana.infli.web.dto.response.ApiResponse;
import com.plana.infli.web.dto.response.post.board.BoardPostsResponse;
import com.plana.infli.web.dto.response.post.image.PostImageUploadResponse;
import com.plana.infli.web.dto.response.post.my.MyPostsResponse;
import com.plana.infli.web.dto.response.post.search.SearchedPostsResponse;
import com.plana.infli.web.dto.response.post.single.SinglePostResponse;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class PostController {

    private final PostService postService;

    @GetMapping("/posts/policy")
    @Operation(summary = "글 작성 이용 규칙 동의 여부 확인")
    public ApiResponse<Boolean> checkMemberAcceptedWritePolicy(
            @AuthenticatedPrincipal String username) {
        return ok(postService.checkMemberAcceptedWritePolicy(username), "true : 동의한 상태");
    }

    @PostMapping("/posts/policy")
    @Operation(summary = "글 작성 이용 규칙 동의함 요청")
    public ApiResponse<Void> acceptWritePolicy(@AuthenticatedPrincipal String username) {
        postService.acceptWritePolicy(username);
        return ok();
    }

    @GetMapping("/boards/{boardId}/permissions")
    @Operation(summary = "특정 게시판에 글 작성 권한이 있는지 여부 확인")
    public ApiResponse<Boolean> checkMemberHasWritePermission(@AuthenticatedPrincipal String username,
            @PathVariable Long boardId, PostType postType) {

        return ok(postService.checkMemberHasWritePermission(boardId, username, postType), "글 작성 가능");
    }

    @PostMapping("/posts/normal")
    @Operation(summary = "일반글 작성")
    public ApiResponse<Long> writeNormalPost(@AuthenticatedPrincipal String username,
            @Validated @RequestBody CreateNormalPostRequest request) {
        return created(postService.createNormalPost(request.toServiceRequest(username)));
    }

    @PostMapping("/posts/recruitment")
    @Operation(summary = "모집글 작성")
    public ApiResponse<Long> writeRecruitmentPost(@AuthenticatedPrincipal String username,
            @Validated @RequestBody CreateRecruitmentPostRequest request) {
        return created(postService.createRecruitmentPost(request.toServiceRequest(username)));
    }


    @PostMapping("/posts/{postId}/images")
    @Operation(summary = "특정 글에 사진 업로드")
    public ApiResponse<PostImageUploadResponse> uploadPostImages(
            @AuthenticatedPrincipal String username, @PathVariable Long postId,
            List<MultipartFile> multipartFiles) {

        return created(postService.uploadPostImages(postId, multipartFiles, username));
    }

    @PatchMapping("/posts/normal")
    @Operation(summary = "내가 작성한 일반글 단건 수정")
    public ApiResponse<String> editNormalPost(@AuthenticatedPrincipal String username,
            @Validated @RequestBody EditNormalPostRequest request) {

        postService.editNormalPost(request.toServiceRequest(username));
        return ok("글 수정 완료");
    }

    @PatchMapping("/posts/recruitment")
    @Operation(summary = "내가 작성한 모집글 단건 수정")
    public ApiResponse<String> editRecruitmentPost(@AuthenticatedPrincipal String username,
            @Validated @RequestBody EditRecruitmentPostRequest request) {

        postService.editRecruitmentPost(request.toServiceRequest(username));
        return ok("글 수정 완료");
    }

    @DeleteMapping("/posts/{postId}")
    @Operation(summary = "글 단건 삭제")
    public ApiResponse<String> delete(@AuthenticatedPrincipal String username, @PathVariable Long postId) {

        postService.deletePost(postId, username);
        return ok("글 삭제 완료");
    }


    @GetMapping("/posts/{postId}")
    @Operation(summary = "글 단건 조회")
    public ApiResponse<SinglePostResponse> loadSinglePost(@AuthenticatedPrincipal String username,
            @PathVariable Long postId) {

        return ok(postService.loadSinglePost(postId, username));
    }

    @GetMapping("/members/posts")
    @Operation(summary = "내가 작성한 글 목록 조회")
    public ApiResponse<MyPostsResponse> loadMyPosts(@RequestParam(defaultValue = "1") Integer page,
            @AuthenticatedPrincipal String username) {

        return ok(postService.loadMyPosts(username, page));
    }

    @GetMapping("/posts/boards/{boardId}")
    @Operation(summary = "특정 게시판에 작성된 글 목록 조회")
    public ApiResponse<BoardPostsResponse> loadsPostsByBoard(@AuthenticatedPrincipal String username,
            @PathVariable Long boardId, @Validated LoadPostsByBoardRequest request) {

        return ok(postService.loadPostsByBoard(request.toServiceRequest(boardId, username)));
    }

    @GetMapping("/posts/search")
    @Operation(summary = "글 키워드로 검색")
    public ApiResponse<SearchedPostsResponse> searchPostsByKeyword(
            @AuthenticatedPrincipal String username, SearchPostsByKeywordRequest request) {

        return ok(postService.searchPostsByKeyword(request.toServiceRequest(username)));
    }
}
