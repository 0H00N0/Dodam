package com.dodam.board;//service

import java.util.List;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    public List<BoardDto> list() {
        return boardRepository.findAll();
    }

    public BoardDto get(Long id) {
        return boardRepository.findById(id).orElse(null);
    }

    public BoardDto create(BoardDto dto) {
        return boardRepository.save(dto);
    }

    public BoardDto update(Long id, BoardDto dto) {
        if (boardRepository.existsById(id)) {
            dto.setId(id);
            return boardRepository.save(dto);
        }
        return null;
    }

    public boolean delete(Long id) {
        if (boardRepository.existsById(id)) {
            boardRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
