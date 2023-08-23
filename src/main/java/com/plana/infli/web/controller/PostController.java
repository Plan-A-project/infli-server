package com.plana.infli.web.controller;

import static org.springframework.http.HttpStatus.*;

import com.plana.infli.domain.type.PostType;
import com.plana.infli.service.PostService;
import com.plana.infli.web.dto.request.post.create.recruitment.CreateRecruitmentPostRequest;
import com.plana.infli.web.dto.request.post.edit.recruitment.EditRecruitmentPostRequest;
import com.plana.infli.web.dto.request.post.view.board.LoadPostsByBoardRequest;
import com.plana.infli.web.dto.request.post.create.normal.CreateNormalPostRequest;
import com.plana.infli.web.dto.request.post.edit.normal.EditNormalPostRequest;
import com.plana.infli.web.dto.request.post.view.search.SearchPostsByKeywordRequest;
import com.plana.infli.web.dto.response.post.board.BoardPostsResponse;
import com.plana.infli.web.dto.response.post.image.PostImageUploadResponse;
import com.plana.infli.web.dto.response.post.my.MyPostsResponse;
import com.plana.infli.web.dto.response.post.search.SearchedPostsResponse;
import com.plana.infli.web.dto.response.post.single.SinglePostResponse;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class PostController {

    private final PostService postService;

    @GetMapping("/boards/{boardId}/permissions")
    @Operation(summary = "특정 게시판에 글 작성 권한이 있는지 여부 확인")
    public boolean checkMemberHasWritePermission(@AuthenticatedPrincipal String username,
            @PathVariable Long boardId, PostType postType) {

        return postService.checkMemberHasWritePermission(boardId, username, postType);
    }

    @PostMapping("/posts/normal")
    @Operation(summary = "일반글 작성")
    @ResponseStatus(CREATED)
    public Long writeNormalPost(@AuthenticatedPrincipal String username,
            @Validated @RequestBody CreateNormalPostRequest request) {
        return postService.createNormalPost(request.toServiceRequest(username));
    }

    @PostMapping("/posts/recruitment")
    @Operation(summary = "모집글 작성")
    @ResponseStatus(CREATED)
    public Long writeRecruitmentPost(@AuthenticatedPrincipal String username,
            @Validated @RequestBody CreateRecruitmentPostRequest request) {
        return postService.createRecruitmentPost(request.toServiceRequest(username));
    }


    @PostMapping("/posts/{postId}/images")
    @Operation(summary = "특정 글에 사진 업로드")
    public PostImageUploadResponse uploadPostImages(
            @AuthenticatedPrincipal String username, @PathVariable Long postId,
            List<MultipartFile> multipartFiles) {

        return postService.uploadPostImages(postId, multipartFiles, username);
    }

    @PatchMapping("/posts/normal")
    @Operation(summary = "내가 작성한 일반글 단건 수정")
    public String editNormalPost(@AuthenticatedPrincipal String username,
            @Validated @RequestBody EditNormalPostRequest request) {

        postService.editNormalPost(request.toServiceRequest(username));
        return "글 수정 완료";
    }

    @PatchMapping("/posts/recruitment")
    @Operation(summary = "내가 작성한 모집글 단건 수정")
    public String editRecruitmentPost(@AuthenticatedPrincipal String username,
            @Validated @RequestBody EditRecruitmentPostRequest request) {

        postService.editRecruitmentPost(request.toServiceRequest(username));
        return "글 수정 완료";
    }

    @DeleteMapping("/posts/{postId}")
    @Operation(summary = "글 단건 삭제")
    public String delete(@AuthenticatedPrincipal String username, @PathVariable Long postId) {

        postService.deletePost(postId, username);
        return "글 삭제 완료";
    }


    @GetMapping("/posts/{postId}")
    @Operation(summary = "글 단건 조회")
    public SinglePostResponse loadSinglePost(@AuthenticatedPrincipal String username,
            @PathVariable Long postId) {

        return postService.loadSinglePost(postId, username);
    }

    @GetMapping("/members/posts")
    @Operation(summary = "내가 작성한 글 목록 조회")
    public MyPostsResponse loadMyPosts(@AuthenticatedPrincipal String username,
            @RequestParam Integer page) {

        return postService.loadMyPosts(username, page);
    }

    @GetMapping("/boards/{boardId}")
    @Operation(summary = "특정 게시판에 작성된 글 목록 조회")
    public BoardPostsResponse loadsPostsByBoard(@AuthenticatedPrincipal String username,
            @PathVariable Long boardId, @Validated LoadPostsByBoardRequest request) {
        return postService.loadPostsByBoard(request.toServiceRequest(boardId, username));
    }

    @GetMapping("/posts/search")
    @Operation(summary = "글 키워드로 검색")
    public SearchedPostsResponse searchPostsByKeyword(
            @AuthenticatedPrincipal String username,
            @Validated SearchPostsByKeywordRequest request) {

        return postService.searchPostsByKeyword(request.toServiceRequest(username));
    }
}
