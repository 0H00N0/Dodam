package com.dodam.member.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberEntity {

    @Id
    @Column(name = "mnum")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mnum;

    // FK: 가입번호
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lmnum", referencedColumnName = "lmunm", nullable = false)
    private LoginmethodEntity loginmethod;

    // FK: 회원타입(0/1/2/3)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mtnum", referencedColumnName = "mtnum", nullable = false)
    private MemtypeEntity memtype;

    @Column(name = "mid",   nullable = false, length = 100)  private String mid;    // ID
    @Column(name = "mpw",   nullable = false, length = 255)  private String mpw;    // BCrypt PW
    @Column(name = "mname", nullable = false, length = 100)  private String mname;  // 이름
    @Column(name = "memail", length = 255)                   private String memail; // 이메일 (NULL 허용)
    @Column(name = "mtel",  nullable = false, length = 30)   private String mtel;   // 전화
    @Column(name = "maddr", nullable = false, length = 255)  private String maddr;  // 상세주소
    @Column(name = "mpost", nullable = false)                private Long mpost;    // 우편번호
    @Column(name = "mbirth", nullable = false)               private LocalDate mbirth; // 생일
    @Column(name = "mreg",   nullable = false)               private LocalDate mreg;   // 가입일(date)
    @Column(name = "mnic",   length = 100)                   private String mnic;   // 닉네임 (NULL)

    // 감사 필드(운영 편의)
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.mreg == null) this.mreg = LocalDate.now();
    }
    @PreUpdate
    void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    /** -------------------------
     *  역할 관련 헬퍼 메서드 추가
     * ------------------------- */
    public String getRoleString() {
        if (memtype == null) return "UNKNOWN";
        return memtype.getRoleName(); // ✅ roleName 가져오기
    }

    public boolean isSuperAdmin() {
        return "SUPERADMIN".equalsIgnoreCase(getRoleString());
    }

    public boolean isStaff() {
        return "STAFF".equalsIgnoreCase(getRoleString());
    }

    public boolean isDeliveryman() {
        return "DELIVERYMAN".equalsIgnoreCase(getRoleString());
    }
}
