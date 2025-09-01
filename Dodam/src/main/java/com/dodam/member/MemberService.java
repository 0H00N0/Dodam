package com.dodam.member;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<MemberEntity> findAll() {
        return memberRepository.findAll();
    }

    public MemberEntity findById(Integer id) {
        return memberRepository.findById(id).orElse(null);
    }

    public MemberEntity save(MemberEntity member) {
        return memberRepository.save(member);
    }

    public void deleteById(Integer id) {
        memberRepository.deleteById(id);
    }
}