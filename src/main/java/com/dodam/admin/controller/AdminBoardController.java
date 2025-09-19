package com.dodam.admin.controller;

import com.dodam.admin.board.dto.AdminBoardDTO.*; 
import com.dodam.admin.service.AdminBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/boards") // Base URL for all admin board-related APIs
@RequiredArgsConstructor
public class AdminBoardController {

    private final AdminBoardService AdminboardService;

    /**
     * 게시글 목록 조회 (GET /api/admin/boards)
     */
    @GetMapping
    public ResponseEntity<PageResponse<BoardListResponse>> getBoardList(BoardSearchRequest searchRequest) {
        PageResponse<BoardListResponse> boardList = AdminboardService.getBoardList(searchRequest);
        return ResponseEntity.ok(boardList);
    }

    /**
     * 게시글 상세 조회 (GET /api/admin/boards/{bnum})
     */
    @GetMapping("/{bnum}")
    public ResponseEntity<BoardDetailResponse> getBoardDetail(@PathVariable Long bnum) {
        BoardDetailResponse boardDetail = AdminboardService.getBoardDetail(bnum);
        return ResponseEntity.ok(boardDetail);
    }

    /**
     * 게시글 수정 (PUT /api/admin/boards/{bnum})
     */
    @PutMapping("/{bnum}")
    public ResponseEntity<BoardDetailResponse> updateBoard(@PathVariable Long bnum, @RequestBody BoardUpdateRequest request) {
        BoardDetailResponse updatedBoard = AdminboardService.updateBoard(bnum, request);
        return ResponseEntity.ok(updatedBoard);
    }

    /**
     * 게시글 삭제 (DELETE /api/admin/boards/{bnum})
     */
    @DeleteMapping("/{bnum}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long bnum) {
        AdminboardService.deleteBoard(bnum);
        return ResponseEntity.noContent().build(); // Return 204 No Content for successful deletion
    }

    /**
     * 게시글 일괄 작업 (POST /api/admin/boards/bulk-actions)
     */
    @PostMapping("/bulk-actions")
    public ResponseEntity<Void> bulkAction(@RequestBody BulkActionRequest request) {
        AdminboardService.bulkAction(request);
        return ResponseEntity.ok().build();
    }

    // --- 카테고리(Category) API ---

    /**
     * 모든 카테고리 조회 (GET /api/admin/boards/categories)
     */
    @GetMapping("/categories")
    public ResponseEntity<List<BoardCategoryResponse>> getAllCategories() {
        List<BoardCategoryResponse> categories = AdminboardService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * 카테고리 생성 (POST /api/admin/boards/categories)
     */
    @PostMapping("/categories")
    public ResponseEntity<BoardCategoryResponse> createCategory(@RequestBody BoardCategoryRequest request) {
        BoardCategoryResponse createdCategory = AdminboardService.createCategory(request);
        return ResponseEntity.ok(createdCategory);
    }

    /**
     * 카테고리 수정 (PUT /api/admin/boards/categories/{bcnum})
     */
    @PutMapping("/categories/{bcnum}")
    public ResponseEntity<BoardCategoryResponse> updateCategory(@PathVariable Long bcnum, @RequestBody BoardCategoryRequest request) {
        BoardCategoryResponse updatedCategory = AdminboardService.updateCategory(bcnum, request);
        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * 카테고리 삭제 (DELETE /api/admin/boards/categories/{bcnum})
     */
    @DeleteMapping("/categories/{bcnum}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long bcnum) {
        AdminboardService.deleteCategory(bcnum);
        return ResponseEntity.noContent().build();
    }

    // --- 상태(State) API ---

    /**
     * 모든 상태 조회 (GET /api/admin/boards/states)
     */
    @GetMapping("/states")
    public ResponseEntity<List<BoardStateResponse>> getAllStates() {
        List<BoardStateResponse> states = AdminboardService.getAllStates();
        return ResponseEntity.ok(states);
    }

    /**
     * 상태 생성 (POST /api/admin/boards/states)
     */
    @PostMapping("/states")
    public ResponseEntity<BoardStateResponse> createState(@RequestBody BoardStateRequest request) {
        BoardStateResponse createdState = AdminboardService.createState(request);
        return ResponseEntity.ok(createdState);
    }

    /**
     * 상태 수정 (PUT /api/admin/boards/states/{bsnum})
     */
    @PutMapping("/states/{bsnum}")
    public ResponseEntity<BoardStateResponse> updateState(@PathVariable Long bsnum, @RequestBody BoardStateRequest request) {
        BoardStateResponse updatedState = AdminboardService.updateState(bsnum, request);
        return ResponseEntity.ok(updatedState);
    }

    /**
     * 상태 삭제 (DELETE /api/admin/boards/states/{bsnum})
     */
    @DeleteMapping("/states/{bsnum}")
    public ResponseEntity<Void> deleteState(@PathVariable Long bsnum) {
        AdminboardService.deleteState(bsnum);
        return ResponseEntity.noContent().build();
    }
}