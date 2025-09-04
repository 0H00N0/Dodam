package com.dodam.board;//

import com.dodam.board.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/boards") @RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    @GetMapping public List<Board> list() { return boardService.findAll(); }
}
