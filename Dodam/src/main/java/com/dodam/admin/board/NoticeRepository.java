package com.dodam.admin.board;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {
    List<NoticeEntity> findByIsActiveTrueOrderByCreatedAtDesc();
    List<NoticeEntity> findAllByOrderByCreatedAtDesc();
    
    default List<NoticeEntity> findTopNOrderByCreatedAtDesc(int n) {
        return findAll(PageRequest.of(0, n, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
    }
    default List<NoticeEntity> findTopNOrderByIdDesc(int n) {
        return findAll(PageRequest.of(0, n, Sort.by(Sort.Direction.DESC, "id"))).getContent();
    }
}
