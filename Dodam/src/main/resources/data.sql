-- 초기 로그인 방법 데이터
INSERT INTO loginmethod (lmtype) VALUES ('local');
INSERT INTO loginmethod (lmtype) VALUES ('naver');
INSERT INTO loginmethod (lmtype) VALUES ('google');
INSERT INTO loginmethod (lmtype) VALUES ('kakao');

-- 초기 회원 타입 데이터
INSERT INTO memtype (role_name) VALUES ('SUPERADMIN');
INSERT INTO memtype (role_name) VALUES ('ADMIN');
INSERT INTO memtype (role_name) VALUES ('STAFF');
INSERT INTO memtype (role_name) VALUES ('DELIVERYMAN');
INSERT INTO memtype (role_name) VALUES ('USER');

-- 초기 관리자 계정 생성 (admin/1234)
INSERT INTO member (
    lmnum, mtnum, mid, mpw, mname, memail, mtel, maddr, mpost, mbirth, mreg, mnic, created_at, updated_at
) VALUES (
    1, -- local login method
    1, -- SUPERADMIN role
    'admin',
    '1234', -- 평문 비밀번호 (실제로는 BCrypt 암호화 필요)
    '시스템관리자',
    'admin@dodam.com',
    '010-0000-0000',
    '서울시 강남구 테헤란로',
    12345,
    '1990-01-01',
    '2024-01-01',
    'admin',
    NOW(),
    NOW()
);