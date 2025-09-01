package com.dodam.admin.board;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bnum;

    private String btitle;
    @Lob // CLOB 타입 매핑
    private String bcontent;
    private String mid;
    private String mnic;

    @CreationTimestamp
    private LocalDateTime bdate;

    @UpdateTimestamp
    private LocalDateTime bedate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bcnum")
    private BoardCategory boardCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bsnum")
    private BoardState boardState;
}
