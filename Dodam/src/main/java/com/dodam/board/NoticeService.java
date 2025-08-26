package com.dodam.board;

import org.springframework.stereotype.Service;
import java.util.List;
import com.dodam.board.NoticeRepository;
import com.dodam.board.NoticeEntity;
import com.dodam.board.NoticeService;
@Service
public class NoticeService {
  private final NoticeRepository repo;
  public NoticeService(NoticeRepository r){this.repo=r;}
  public List<NoticeEntity> latest(){ return repo.findTop5ByOrderByPinnedDescCreatedAtDesc(); }
  public List<NoticeEntity> list(){ return repo.findAll(); }
}