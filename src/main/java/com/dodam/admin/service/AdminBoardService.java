package com.dodam.admin.service;

import com.dodam.admin.board.dto.AdminBoardDTO.*;
import com.dodam.board.entity.BoardEntity;
import com.dodam.board.entity.BoardCategoryEntity;
import com.dodam.board.entity.BoardStateEntity;
import com.dodam.admin.board.repository.BoardRepository;
import com.dodam.admin.board.repository.BoardCategoryRepository;
import com.dodam.admin.board.repository.BoardStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdminBoardService {

    private final BoardRepository boardRepository;
    private final BoardCategoryRepository boardCategoryRepository;
    private final BoardStateRepository boardStateRepository;

    /**
     * 게시글 목록 조회 (검색 및 필터링 포함)
     */
    @Transactional(readOnly = true)
    public PageResponse<BoardListResponse> getBoardList(BoardSearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(
            searchRequest.getPage(), 
            searchRequest.getSize(),
            Sort.by(Sort.Direction.fromString(searchRequest.getSortDir()), searchRequest.getSortBy())
        );

        Page<BoardEntity> boardPage = boardRepository.findBoardsWithFilters(
            searchRequest.getKeyword(),
            searchRequest.getSearchType(),
            searchRequest.getBcnum(),
            searchRequest.getBsnum(),
            searchRequest.getStartDate(),
            searchRequest.getEndDate(),
            pageable
        );

        List<BoardListResponse> content = boardPage.getContent().stream()
            .map(this::convertToBoardListResponse)
            .collect(Collectors.toList());

        return PageResponse.<BoardListResponse>builder()
            .content(content)
            .page(boardPage.getNumber())
            .size(boardPage.getSize())
            .totalElements(boardPage.getTotalElements())
            .totalPages(boardPage.getTotalPages())
            .first(boardPage.isFirst())
            .last(boardPage.isLast())
            .build();
    }

    /**
     * 게시글 상세 조회
     */
    @Transactional(readOnly = true)
    public BoardDetailResponse getBoardDetail(Long bnum) {
        BoardEntity board = boardRepository.findByIdWithCategoryAndState(bnum)
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        
        return convertToBoardDetailResponse(board);
    }

    /**
     * 게시글 수정
     */
    public BoardDetailResponse updateBoard(Long bnum, BoardUpdateRequest request) {
        BoardEntity board = boardRepository.findById(bnum)
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 카테고리 변경
        if (request.getBcnum() != null) {
            BoardCategoryEntity category = boardCategoryRepository.findById(request.getBcnum())
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
            board.setBoardCategory(category);
        }

        // 상태 변경
        if (request.getBsnum() != null) {
            BoardStateEntity state = boardStateRepository.findById(request.getBsnum())
                .orElseThrow(() -> new RuntimeException("상태를 찾을 수 없습니다."));
            board.setBoardState(state);
        }

        // 제목, 내용 수정
        if (request.getBtitle() != null) {
            board.setBtitle(request.getBtitle());
        }
        if (request.getBcontent() != null) {
            board.setBcontent(request.getBcontent());
        }

        BoardEntity updatedBoard = boardRepository.save(board);
        return convertToBoardDetailResponse(updatedBoard);
    }

    /**
     * 게시글 삭제
     */
    public void deleteBoard(Long bnum) {
        if (!boardRepository.existsById(bnum)) {
            throw new RuntimeException("게시글을 찾을 수 없습니다.");
        }
        boardRepository.deleteById(bnum);
        log.info("게시글 삭제 완료: {}", bnum);
    }

    /**
     * 게시글 일괄 작업
     */
    public void bulkAction(BulkActionRequest request) {
        List<BoardEntity> boards = boardRepository.findAllById(request.getBoardIds());
        
        switch (request.getAction()) {
            case "DELETE":
                boardRepository.deleteAllById(request.getBoardIds());
                log.info("게시글 일괄 삭제 완료: {} 건", request.getBoardIds().size());
                break;
                
            case "CHANGE_STATE":
                BoardStateEntity newState = boardStateRepository.findById(request.getTargetStateId())
                    .orElseThrow(() -> new RuntimeException("상태를 찾을 수 없습니다."));
                boards.forEach(board -> board.setBoardState(newState));
                boardRepository.saveAll(boards);
                log.info("게시글 상태 일괄 변경 완료: {} 건", boards.size());
                break;
                
            case "CHANGE_CATEGORY":
                BoardCategoryEntity newCategory = boardCategoryRepository.findById(request.getTargetCategoryId())
                    .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
                boards.forEach(board -> board.setBoardCategory(newCategory));
                boardRepository.saveAll(boards);
                log.info("게시글 카테고리 일괄 변경 완료: {} 건", boards.size());
                break;
                
            default:
                throw new RuntimeException("지원하지 않는 작업입니다: " + request.getAction());
        }
    }

    /**
     * 카테고리 목록 조회
     */
    @Transactional(readOnly = true)
    public List<BoardCategoryResponse> getAllCategories() {
        return boardCategoryRepository.findAll().stream() // ✨ 변경점: findAllWithBoardCount() -> findAll()
            .map(this::convertToBoardCategoryResponse)
            .collect(Collectors.toList());
    }

    /**
     * 카테고리 생성
     */
    public BoardCategoryResponse createCategory(BoardCategoryRequest request) {
        // 중복 체크
        if (boardCategoryRepository.existsByBcname(request.getBcname())) {
            throw new RuntimeException("이미 존재하는 카테고리명입니다.");
        }

        BoardCategoryEntity category = new BoardCategoryEntity();
        category.setBcname(request.getBcname());
        
        BoardCategoryEntity savedCategory = boardCategoryRepository.save(category);
        return convertToBoardCategoryResponse(savedCategory);
    }

    /**
     * 카테고리 수정
     */
    public BoardCategoryResponse updateCategory(Long bcnum, BoardCategoryRequest request) {
        BoardCategoryEntity category = boardCategoryRepository.findById(bcnum)
            .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));

        // 중복 체크 (자기 자신 제외)
        if (boardCategoryRepository.existsByBcnameAndBcnumNot(request.getBcname(), bcnum)) {
            throw new RuntimeException("이미 존재하는 카테고리명입니다.");
        }

        category.setBcname(request.getBcname());
        BoardCategoryEntity updatedCategory = boardCategoryRepository.save(category);
        return convertToBoardCategoryResponse(updatedCategory);
    }

    /**
     * 카테고리 삭제
     */
    public void deleteCategory(Long bcnum) {
        BoardCategoryEntity category = boardCategoryRepository.findById(bcnum)
            .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));

        // 해당 카테고리를 사용하는 게시글 수 직접 계산 (데이터 무결성을 위한 로직)
        long boardCount = category.getBoards().size();
        if (boardCount > 0) {
            throw new RuntimeException("해당 카테고리를 사용하는 게시글이 " + boardCount + "개 있어 삭제할 수 없습니다.");
        }

        boardCategoryRepository.delete(category);
        log.info("카테고리 삭제 완료: {}", category.getBcname());
    }

    /**
     * 상태 목록 조회
     */
    @Transactional(readOnly = true)
    public List<BoardStateResponse> getAllStates() {
        return boardStateRepository.findAll().stream() // ✨ 변경점: findAllWithBoardCount() -> findAll()
            .map(this::convertToBoardStateResponse)
            .collect(Collectors.toList());
    }

    /**
     * 상태 생성
     */
    public BoardStateResponse createState(BoardStateRequest request) {
        // 중복 체크
        if (boardStateRepository.existsByBsname(request.getBsname())) {
            throw new RuntimeException("이미 존재하는 상태명입니다.");
        }

        BoardStateEntity state = new BoardStateEntity();
        state.setBsname(request.getBsname());
        
        BoardStateEntity savedState = boardStateRepository.save(state);
        return convertToBoardStateResponse(savedState);
    }

    /**
     * 상태 수정
     */
    public BoardStateResponse updateState(Long bsnum, BoardStateRequest request) {
        BoardStateEntity state = boardStateRepository.findById(bsnum)
            .orElseThrow(() -> new RuntimeException("상태를 찾을 수 없습니다."));

        // 중복 체크 (자기 자신 제외)
        if (boardStateRepository.existsByBsnameAndBsnumNot(request.getBsname(), bsnum)) {
            throw new RuntimeException("이미 존재하는 상태명입니다.");
        }

        state.setBsname(request.getBsname());
        BoardStateEntity updatedState = boardStateRepository.save(state);
        return convertToBoardStateResponse(updatedState);
    }

    /**
     * 상태 삭제
     */
    public void deleteState(Long bsnum) {
        BoardStateEntity state = boardStateRepository.findById(bsnum)
            .orElseThrow(() -> new RuntimeException("상태를 찾을 수 없습니다."));

        // 해당 상태를 사용하는 게시글 수 직접 계산 (데이터 무결성을 위한 로직)
        long boardCount = state.getBoards().size();
        if (boardCount > 0) {
            throw new RuntimeException("해당 상태를 사용하는 게시글이 " + boardCount + "개 있어 삭제할 수 없습니다.");
        }

        boardStateRepository.delete(state);
        log.info("상태 삭제 완료: {}", state.getBsname());
    }

    // === 변환 메서드들 ===

    private BoardListResponse convertToBoardListResponse(BoardEntity board) {
        return BoardListResponse.builder()
            .bnum(board.getBnum())
            .btitle(board.getBtitle())
            .mid(board.getMid())
            .mnic(board.getMnic())
            .categoryName(board.getBoardCategory() != null ? board.getBoardCategory().getBcname() : null)
            .stateName(board.getBoardState() != null ? board.getBoardState().getBsname() : null)
            .bdate(board.getBdate())
            .bedate(board.getBedate())
            .build();
    }

    private BoardDetailResponse convertToBoardDetailResponse(BoardEntity board) {
        return BoardDetailResponse.builder()
            .bnum(board.getBnum())
            .mnum(board.getMnum())
            .tnum(board.getTnum())
            .btitle(board.getBtitle())
            .bcontent(board.getBcontent())
            .mid(board.getMid())
            .mnic(board.getMnic())
            .bdate(board.getBdate())
            .bedate(board.getBedate())
            .category(board.getBoardCategory() != null ? convertToBoardCategoryResponse(board.getBoardCategory()) : null)
            .state(board.getBoardState() != null ? convertToBoardStateResponse(board.getBoardState()) : null)
            .build();
    }

    private BoardCategoryResponse convertToBoardCategoryResponse(BoardCategoryEntity category) {
        return BoardCategoryResponse.builder()
            .bcnum(category.getBcnum())
            .bcname(category.getBcname())
            // ✨ 변경점: .boardCount(...) 라인 삭제
            .build();
    }

    private BoardStateResponse convertToBoardStateResponse(BoardStateEntity state) {
        return BoardStateResponse.builder()
            .bsnum(state.getBsnum())
            .bsname(state.getBsname())
            // ✨ 변경점: .boardCount(...) 라인 삭제
            .build();
    }
}