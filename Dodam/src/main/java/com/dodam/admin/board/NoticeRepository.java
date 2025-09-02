package com.dodam.admin.board;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {
    List<NoticeEntity> findByIsActiveTrueOrderByCreatedAtDesc();
    List<NoticeEntity> findAllByOrderByCreatedAtDesc();
}