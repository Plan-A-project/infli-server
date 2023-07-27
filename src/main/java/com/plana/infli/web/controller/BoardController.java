package com.plana.infli.web.controller;

import static org.springframework.http.ResponseEntity.*;

import com.plana.infli.domain.PostType;
import com.plana.infli.service.BoardService;
import com.plana.infli.web.dto.request.board.popular.enable.controller.ChangePopularBoardVisibilityRequest;
import com.plana.infli.web.dto.request.board.popular.edit.controller.EditPopularBoardSequenceRequest;
import com.plana.infli.web.dto.response.board.settings.board.BoardListResponse;
import com.plana.infli.web.dto.response.board.settings.polularboard.PopularBoardsSettingsResponse;
import com.plana.infli.web.dto.response.board.view.PopularBoardsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Board", description = "게시판 API")
@RequestMapping("/api")
public class BoardController {

    private final BoardService boardService;

    @GetMapping("/boards/popular/exists")
    @Operation(summary = "인기 게시판 기본 설정값 존재하는지 확인")
    @ApiResponse(responseCode = "200", description = "True : 인기 게시판 기본 설정값 존재함, False : 기본 설정값 존재하지 않음" )
    @ApiResponse(responseCode = "404", description = "사용자를 찾을수 없음")
    @ApiResponse(responseCode = "401", description = "로그인을 하지 않은 상태")
    public boolean isPopularBoardCreated(@AuthenticationPrincipal String email) {

        return boardService.popularBoardExistsBy(email);
    }


    @GetMapping("/boards/popular")
    @Operation(summary = "해당 사용자가 보고싶다고 설정한 인기 게시판 목록 조회")
    @ApiResponse(responseCode = "200", description = "인기 게시판 조회 완료",
            content = @Content(schema = @Schema(implementation = PopularBoardsResponse.class)))
    @ApiResponse(responseCode = "400", description = "인기 게시판 설정을 하지 않음")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을수 없음")
    @ApiResponse(responseCode = "401", description = "로그인을 하지 않은 상태")
    public PopularBoardsResponse loadEnabledPopularBoards(@AuthenticationPrincipal String email) {
        return boardService.loadEnabledPopularBoardsBy(email);
    }

    @PostMapping("/boards/popular")
    @Operation(summary = "인기 게시판 기본 설정값 생성")
    @ApiResponse(responseCode = "200", description = "인기 게시판 기본 설정값 생성 완료")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을수 없음")
    @ApiResponse(responseCode = "409", description = "인기 게시판이 이미 설정 되어 있음")
    @ApiResponse(responseCode = "401", description = "로그인을 하지 않은 상태")
    public void createDefaultPopularBoards(@AuthenticationPrincipal String email) {
        boardService.createDefaultPopularBoards(email);
    }

    /**
     * 홈설정 기능 API 시작
     */

    @GetMapping("/settings/boards/popular")
    @Operation(description = "인기 게시판을 회원이 보고 싶은 순서대로 변경하기 위해, 인기 게시판 목록 조회")
    @ApiResponse(responseCode = "200", description = "인기 게시판 목록 조회 완료",
            content = @Content(schema = @Schema(implementation = PopularBoardsSettingsResponse.class)))
    @ApiResponse(responseCode = "404", description = "사용자를 찾을수 없음")
    @ApiResponse(responseCode = "401", description = "로그인을 하지 않은 상태")
    public PopularBoardsSettingsResponse loadEnabledPopularBoardsForSetting(
            @AuthenticationPrincipal String email) {

        return boardService.loadEnabledPopularBoardsForSettingBy(email);
    }

    @PatchMapping("/settings/boards/popular")
    @Operation(description = "인기 게시판을 회원이 보고 싶은 순서대로 변경")
    @ApiResponse(responseCode = "200", description = "순서 변경 완료")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을수 없거나 인기 게시판이 존재하지 않음")
    @ApiResponse(responseCode = "400", description = "DB에 존재하는 특정 회원의 인기 게시판 갯수와, 클라이언트가 전송한 인기 게시판 갯수가 일치하지 않음")
    @ApiResponse(responseCode = "401", description = "로그인을 하지 않은 상태")
    public void changePopularBoardSequence(
            @RequestBody @Validated
            @Parameter(description = "각 인기 게시판들을 보고싶은 순서대로 List에 담는다", required = true) EditPopularBoardSequenceRequest request,
            @AuthenticationPrincipal String email) {
        boardService.changePopularBoardSequence(request.toServiceRequest(), email);
    }


    @GetMapping("/settings/boards")
    @Operation(description = "해당 대학에 존재하는 모든 게시판 조회")
    @ApiResponse(responseCode = "200", description = "모든 게시판 조회 완료")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을수 없음")
    @ApiResponse(responseCode = "401", description = "로그인을 하지 않은 상태")
    public BoardListResponse listAllBoards(@AuthenticationPrincipal String email) {

        return boardService.loadAllBoard(email);
    }

    @PostMapping("/settings/boards/popular")
    @Operation(description = "모든 인기 게시판중 보고싶은 인기 게시판만 조회되도록 선택")
    @ApiResponse(responseCode = "200", description = "설정 완료")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을수 없음")
    @ApiResponse(responseCode = "400", description = "보고싶다고 설정한 인기 게시판의 ID 번호 정보가 잘못됨")
    @ApiResponse(responseCode = "401", description = "로그인을 하지 않은 상태")
    public void changeBoardVisibility(
            @RequestBody @Validated ChangePopularBoardVisibilityRequest request,
            @AuthenticationPrincipal String email) {

        boardService.changeBoardVisibility(request.toServiceRequest(), email);
    }
}
