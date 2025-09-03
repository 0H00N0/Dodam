package com.dodam.admin.board.repository;

import com.dodam.board.entity.BoardCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardCategoryRepository extends JpaRepository<BoardCategoryEntity, Long> {

    /**
     * 카테고리명 중복 체크
     */
    boolean existsByBcname(String bcname);

    /**
     * 카테고리명 중복 체크 (특정 ID 제외)
     */
    boolean existsByBcnameAndBcnumNot(String bcname, Long bcnum);

    /**
     * 모든 카테고리와 게시글 수 조회
     */
    @Query("SELECT bc FROM BoardCategoryEntity bc LEFT JOIN FETCH bc.boards")
    List<BoardCategoryEntity> findAllWithBoardCount();
}