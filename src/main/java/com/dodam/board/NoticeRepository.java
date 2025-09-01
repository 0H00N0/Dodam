package com.dodam.board;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {

    List<NoticeEntity> findTop5ByOrderByPinnedDescCreatedAtDesc();

}
