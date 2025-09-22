package com.dodam.board.repository;

import com.dodam.board.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    /**
     * 특정 게시판 카테고리(bcnum)에 속한 모든 게시글을 찾습니다.
     * @param bcnum 게시판 카테고리 ID
     * @return 게시글 엔티티 리스트
     */
    List<BoardEntity> findByBoardCategory_BcnumOrderByBnumDesc(Long bcnum);
}
