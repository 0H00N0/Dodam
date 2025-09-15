package com.dodam.admin.board;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@Repository
public interface AdminNoticeRepository extends JpaRepository<AdminNoticeEntity, Long> {
    List<AdminNoticeEntity> findByIsActiveTrueOrderByCreatedAtDesc();
    List<AdminNoticeEntity> findAllByOrderByCreatedAtDesc();
    
    default List<AdminNoticeEntity> findTopNOrderByCreatedAtDesc(int n) {
        return findAll(PageRequest.of(0, n, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
    }
    default List<AdminNoticeEntity> findTopNOrderByIdDesc(int n) {
        return findAll(PageRequest.of(0, n, Sort.by(Sort.Direction.DESC, "id"))).getContent();
    }
}
