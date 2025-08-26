package com.dodam.board;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.dodam.board.NoticeService;

@Controller @RequestMapping("/notices")
public class NoticeController {
  private final NoticeService service;
  public NoticeController(NoticeService s){this.service=s;}
  @GetMapping
  public String list(Model model){
    model.addAttribute("items", service.list());
    return "notice/list";
  }
}