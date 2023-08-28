package com.plana.infli.web.controller;

import com.plana.infli.service.BoardService;
import com.plana.infli.web.dto.request.board.popular.enable.ChangePopularBoardVisibilityRequest;
import com.plana.infli.web.dto.request.board.popular.edit.EditPopularBoardSequenceRequest;
import com.plana.infli.web.dto.response.board.settings.board.BoardListResponse;
import com.plana.infli.web.dto.response.board.settings.polularboard.PopularBoardsSettingsResponse;
import com.plana.infli.web.dto.response.board.view.PopularBoardsResponse;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Board", description = "게시판 API")
public class BoardController {

    private final BoardService boardService;

    @GetMapping("/boards/popular/exists")
    @Operation(summary = "인기 게시판 기본 설정값 존재하는지 확인")
    public boolean isPopularBoardCreated(@AuthenticatedPrincipal String username) {
        return boardService.popularBoardExistsBy(username);
    }


    @GetMapping("/boards/popular")
    @Operation(summary = "해당 사용자가 보고싶다고 설정한 인기 게시판 목록 조회")
    public PopularBoardsResponse loadEnabledPopularBoards(@AuthenticatedPrincipal String username) {
        return boardService.loadEnabledPopularBoardsBy(username);
    }

    @PostMapping("/boards/popular")
    @Operation(summary = "인기 게시판 기본 설정값 생성")
    public void createDefaultPopularBoards(@AuthenticatedPrincipal String username) {
        boardService.createDefaultPopularBoards(username);
    }


    /**
     * 홈설정 기능
     */

    @GetMapping("/settings/boards/popular")
    @Operation(description = "인기 게시판을 회원이 보고 싶은 순서대로 변경하기 위해, 인기 게시판 목록 조회")
    public PopularBoardsSettingsResponse loadEnabledPopularBoardsForSetting(
            @AuthenticatedPrincipal String username) {
        return boardService.loadEnabledPopularBoardsForSettingBy(username);
    }

    @PatchMapping("/settings/boards/popular")
    @Operation(description = "인기 게시판을 회원이 보고 싶은 순서대로 변경")
    public void changePopularBoardSequence(@AuthenticatedPrincipal String username,
            @RequestBody @Validated EditPopularBoardSequenceRequest request) {
        boardService.changePopularBoardSequence(request.toServiceRequest(username));
    }


    @GetMapping("/settings/boards")
    @Operation(description = "해당 대학에 존재하는 모든 게시판 조회")
    public BoardListResponse listAllBoards(@AuthenticatedPrincipal String username) {

        return boardService.loadAllBoard(username);
    }

    @PostMapping("/settings/boards/popular")
    @Operation(description = "모든 인기 게시판중 보고싶은 인기 게시판만 조회되도록 선택")
    public void changeBoardVisibility(@AuthenticatedPrincipal String username,
            @RequestBody @Validated ChangePopularBoardVisibilityRequest request) {

        boardService.changeBoardVisibility(request.toServiceRequest(username));

    }
}
