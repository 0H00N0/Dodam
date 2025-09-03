package com.dodam.admin.board.repository;

import com.dodam.board.entity.BoardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, Long> {

    /**
     * 게시글 검색 및 필터링 (복합 조건)
     */
    @Query("SELECT b FROM BoardEntity b " +
           "LEFT JOIN FETCH b.boardCategory bc " +
           "LEFT JOIN FETCH b.boardState bs " +
           "WHERE (:keyword IS NULL OR " +
           "       (:searchType = 'title' AND b.btitle LIKE %:keyword%) OR " +
           "       (:searchType = 'content' AND b.bcontent LIKE %:keyword%) OR " +
           "       (:searchType = 'author' AND (b.mid LIKE %:keyword% OR b.mnic LIKE %:keyword%)) OR " +
           "       (:searchType = 'all' AND (b.btitle LIKE %:keyword% OR b.bcontent LIKE %:keyword% OR b.mid LIKE %:keyword% OR b.mnic LIKE %:keyword%))) " +
           "AND (:bcnum IS NULL OR b.boardCategory.bcnum = :bcnum) " +
           "AND (:bsnum IS NULL OR b.boardState.bsnum = :bsnum) " +
           "AND (:startDate IS NULL OR b.bdate >= :startDate) " +
           "AND (:endDate IS NULL OR b.bdate <= :endDate)")
    Page<BoardEntity> findBoardsWithFilters(
            @Param("keyword") String keyword,
            @Param("searchType") String searchType,
            @Param("bcnum") Long bcnum,
            @Param("bsnum") Long bsnum,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * 게시글 상세 조회 (카테고리, 상태 포함)
     */
    @Query("SELECT b FROM BoardEntity b " +
           "LEFT JOIN FETCH b.boardCategory " +
           "LEFT JOIN FETCH b.boardState " +
           "WHERE b.bnum = :bnum")
    Optional<BoardEntity> findByIdWithCategoryAndState(@Param("bnum") Long bnum);
}