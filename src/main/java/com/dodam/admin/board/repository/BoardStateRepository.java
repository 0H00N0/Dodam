package com.dodam.admin.board.repository;

import com.dodam.board.entity.BoardStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardStateRepository extends JpaRepository<BoardStateEntity, Long> {

	
    /**
     * 상태명 중복 체크
     */
    boolean existsByBsname(String bsname);

    /**
     * 상태명 중복 체크 (특정 ID 제외)
     */
 // In BoardStateRepository.java
    boolean existsByBsnameAndBsnumNot(String bsname, Long bsnum);

    /**
     * 모든 상태와 게시글 수 조회
     */
    @Query("SELECT bs FROM BoardStateEntity bs LEFT JOIN FETCH bs.boards")
    List<BoardStateEntity> findAllWithBoardCount();
}