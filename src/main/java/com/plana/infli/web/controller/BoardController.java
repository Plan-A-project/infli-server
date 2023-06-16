package com.plana.infli.web.controller;

import static org.springframework.http.HttpStatus.*;

import com.plana.infli.service.BoardService;
import com.plana.infli.service.validator.board.CreateBoardValidator;
import com.plana.infli.service.validator.board.CreateMemberBoardValidator;
import com.plana.infli.service.validator.board.EditMemberBoardValidator;
import com.plana.infli.web.dto.request.board.CreateBoardRequest;
import com.plana.infli.web.dto.request.board.CreateMemberBoardRequest;
import com.plana.infli.web.dto.request.board.EditMemberBoardRequest;
import com.plana.infli.web.dto.response.board.all.BoardListResponse;
import com.plana.infli.web.dto.response.board.member.MemberBoardListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "board", description = "게시판 API")
public class BoardController {

    private final BoardService boardService;

    private final CreateBoardValidator createBoardValidator;

    private final CreateMemberBoardValidator createMemberBoardValidator;

    private final EditMemberBoardValidator editMemberBoardValidator;

    @InitBinder("createBoardRequest")
    public void createBoardBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(createBoardValidator);
    }

    @InitBinder("createMemberBoardRequest")
    public void createMemberBoardBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(createMemberBoardValidator);
    }

    @InitBinder("editMemberBoardRequest")
    public void editMemberBoardBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(editMemberBoardValidator);
    }

    @GetMapping("/api/admin/boards/validate/{boardName}")
    @Operation(description = "게시판 이름 중복 여부 확인")
    @ApiResponse(responseCode = "200", description = "해당 게시판 이름 사용 가능")
    @ApiResponse(responseCode = "409", description = "해당 이름으로 게시판 생성할수 없음")
    public ResponseEntity<Void> checkBoardNameAvailable(
            @Parameter(description = "중복 여부 확인이 필요한 게시판 이름") @PathVariable String boardName) {
        if (boardService.existsByBoardName(boardName)) {
            return ResponseEntity.status(CONFLICT).build();
        } else {
            return ResponseEntity.ok().build();
        }
    }

    @PostMapping("/api/admin/boards")
    @Operation(description = "새로운 게시판 생성")
    @ApiResponse(responseCode = "201", description = "새로운 게시판 생성 완료")
    @ApiResponse(responseCode = "409", description = "동일한 이름의 게시판이 존재")
    public ResponseEntity<Void> create(@RequestBody @Validated CreateBoardRequest request) {

        boardService.create(request);

        return ResponseEntity.ok().build();
    }


    @GetMapping("/api/boards")
    @Operation(description = "해당 대학에 존재하는 모든 게시판 조회")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = BoardListResponse.class)))
    public ResponseEntity<BoardListResponse> boardList() {

        BoardListResponse response = boardService.findAllExistingBoard();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/api/memberBoard")
    @Operation(description = "특정 회원이 보고싶어하는 게시판 조회")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = MemberBoardListResponse.class)))
    public ResponseEntity<MemberBoardListResponse> memberBoardList() {

        MemberBoardListResponse response = boardService.findAllMemberBoard();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/memberBoards")
    @Operation(description = "회원이 보고 싶어하는 게시판 생성")
    @ApiResponse(responseCode = "201", description = "생성 완료")
    public ResponseEntity<Void> createMemberBoard(
            @RequestBody @Validated CreateMemberBoardRequest request) {

        boardService.createMemberBoard(request);

        return ResponseEntity.ok().build();
    }


    @PatchMapping("/api/memberBoards")
    @Operation(description = "회원이 보고싶어 하는 게시판 정렬 순서 변경")
    @ApiResponse(responseCode = "200", description = "순서 변경 완료")
    public void editMemberBoardOrder(@RequestBody @Validated EditMemberBoardRequest request) {
        boardService.edit(request);
    }


    @DeleteMapping("/api/admin/boards")
    @Operation(description = "게시판 삭제")
    @ApiResponse(responseCode = "200", description = "정상적으로 삭제 완료")
    @ApiResponse(responseCode = "404", description = "게시판이 이미 삭제되었거나, 존재하지 않는 경우")
    public void delete(@RequestParam Long boardId) {
        boardService.delete(boardId);
    }
}
