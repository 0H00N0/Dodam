package com.dodam.board;//service
	
import com.dodam.board.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class BoardService {
    private final BoardRepository boardRepository;
    public List<Board> findAll() { return boardRepository.findAll(); }
    @Transactional
    public Board getOrCreate(String code, String name, String description) {
        return boardRepository.findByCode(code)
            .orElseGet(() -> boardRepository.save(Board.builder()
                .code(code).name(name).description(description).build()));
    }
    public Board getByCode(String code) {
        return boardRepository.findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Board code not found: " + code));
    }
}