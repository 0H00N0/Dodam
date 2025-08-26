package com.dodam.board;
			
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.dodam.board.NoticeEntity;

public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {
  List<NoticeEntity> findTop5ByOrderByPinnedDescCreatedAtDesc();
}//