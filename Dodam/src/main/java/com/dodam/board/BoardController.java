package com.dodam.board;//controller

import org.springframework.web.bind.annotation.*;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @GetMapping
    public List<BoardDto> list() {
        return boardService.list();
    }

    @GetMapping("/{id}")
    public BoardDto get(@PathVariable Long id) {
        return boardService.get(id);
    }

    @PostMapping
    public BoardDto create(@RequestBody BoardDto dto) {
        return boardService.create(dto);
    }

    @PutMapping("/{id}")
    public BoardDto update(@PathVariable Long id, @RequestBody BoardDto dto) {
        return boardService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return boardService.delete(id);
    }
}
