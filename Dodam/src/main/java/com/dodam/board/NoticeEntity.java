package com.dodam.board;

import jakarta.persistence.*; 
import java.time.LocalDateTime;
import com.dodam.board.NoticeController;				
		

@Entity 
@Table(name="notices")
public class NoticeEntity {
  @Id 
  @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable=false, length=200) private String title;
  @Lob 
  @Column(nullable=false) private String content;
  @Column(nullable=false) private LocalDateTime createdAt = LocalDateTime.now();
  private Boolean pinned = false;
  public Long getId(){return id;} public void setId(Long v){id=v;}
  public String getTitle(){return title;} public void setTitle(String v){title=v;}
  public String getContent(){return content;} public void setContent(String v){content=v;}
  public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
  public Boolean getPinned(){return pinned;} public void setPinned(Boolean v){pinned=v;}
}