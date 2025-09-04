package com.dodam.board.service;
import com.dodam.board.BoardEntity; 
import com.dodam.board.dto.BoardDto; 
import com.dodam.board.repository.BoardRepository; 
import com.dodam.board.error.NotFoundException;
import lombok.RequiredArgsConstructor; 
import org.springframework.stereotype.Service; 
import java.util.List;
@Service @RequiredArgsConstructor public class BoardService {
  private final BoardRepository repo;
  public List<BoardDto> list(){ return repo.findAll()
		  .stream().map(b->BoardDto.builder()
				  .code(b.getCode()).name(b.getName())
				  .description(b.getDescription()).build()).toList(); }
  public BoardEntity getByCodeOrThrow(String code){ 
	  return repo.findByCode(code)
			  .orElseThrow(()->new NotFoundException("보드 코드를 찾을 수 없습니다: "+code)); }
}