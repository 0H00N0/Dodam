package com.dodam.admin.board.notice;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "board") // 실제 DB의 'board' 테이블과 매핑됩니다.
public class NoticeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bnum")
    private Long bnum; // 게시글 번호 (Primary Key)

    @Column(name = "bsub", nullable = false, length = 255)
    private String bsub; // 제목

    @Column(name = "bcontent", nullable = false, length = 2000)
    private String bcontent; // 내용

    @Column(name = "mid", nullable = false)
    private String mid; // 작성자 ID

    @Column(name = "mnic", nullable = false)
    private String mnic; // 작성자 닉네임

    @CreationTimestamp // JPA 엔티티가 생성될 때의 시간을 자동으로 저장합니다.
    @Column(name = "bdate", updatable = false)
    private LocalDateTime bdate; // 작성일

    @UpdateTimestamp // JPA 엔티티가 수정될 때의 시간을 자동으로 저장합니다.
    @Column(name = "bedate")
    private LocalDateTime bedate; // 수정일

    // --- DB 명세에 있는 다른 컬럼들도 필요에 따라 추가할 수 있습니다. ---
    // 예: 카테고리, 상태, 회원번호 등
    // @Column(name = "bcanum")
    // private Long bcanum; // 카테고리 번호

    // @Column(name = "bsnum")
    // private Long bsnum; // 상태 번호
}


