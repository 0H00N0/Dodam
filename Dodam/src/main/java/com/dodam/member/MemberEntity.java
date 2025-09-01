package com.dodam.member;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Data
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mnum")
    private Integer mnum;

    @Column(name = "mid", unique = true, nullable = false)
    private String mid;

    @Column(name = "mpw", nullable = false)
    private String mpw;

    @Column(name = "mname", nullable = false)
    private String mname;

    @Column(name = "memail", nullable = false)
    private String memail;

    @Column(name = "mphone")
    private String mphone;

    @Column(name = "mbirth")
    private LocalDate mbirth;

    @Column(name = "mzipcode")
    private String mzipcode;

    @Column(name = "maddress1")
    private String maddress1;

    @Column(name = "maddress2")
    private String maddress2;

    @Column(name = "mjoindate", nullable = false, updatable = false)
    private LocalDateTime mjoindate;

    @Column(name = "mgrade")
    private String mgrade;

    @Column(name = "mtype")
    private String mtype;

    @Column(name = "mprovider")
    private String mprovider;

    @PrePersist
    protected void onCreate() {
        mjoindate = LocalDateTime.now();
    }
}

