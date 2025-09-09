-- PlanData.sql (CamelCase only, H2 2.x)
-- 작성: 2025-09-03 KST
-- 전제: 테이블명이 CamelCase (planName / plans / planBenefit / planTerms / planPrice)
-- 주의: IDENTITY/PK 컬럼은 DB가 자동 생성 → MERGE 시 ID 컬럼을 명시하지 않음

/* 1) 플랜 이름 사전 (중복이면 MERGE로 upsert) */
MERGE INTO PLANNAME (PLANNAME) KEY (PLANNAME)
VALUES ('베이직'),
       ('스탠다드'),
       ('프리미엄'),
       ('패밀리'),
       ('VIP');

/* 2) 플랜 마스터: planCode는 카멜/파스칼 케이스로 저장 ('Basic' 등) */
INSERT INTO PLANS (PLANNAMEID, PLANCODE, PLANACTIVE, PLANCREATE)
SELECT PN.PLANNAMEID, 'Basic', TRUE, CURRENT_TIMESTAMP
FROM PLANNAME PN
WHERE PN.PLANNAME = '베이직'
  AND NOT EXISTS (SELECT 1 FROM PLANS P WHERE P.PLANCODE = 'Basic');

INSERT INTO PLANS (PLANNAMEID, PLANCODE, PLANACTIVE, PLANCREATE)
SELECT PN.PLANNAMEID, 'Standard', TRUE, CURRENT_TIMESTAMP
FROM PLANNAME PN
WHERE PN.PLANNAME = '스탠다드'
  AND NOT EXISTS (SELECT 1 FROM PLANS P WHERE P.PLANCODE = 'Standard');

INSERT INTO PLANS (PLANNAMEID, PLANCODE, PLANACTIVE, PLANCREATE)
SELECT PN.PLANNAMEID, 'Premium', TRUE, CURRENT_TIMESTAMP
FROM PLANNAME PN
WHERE PN.PLANNAME = '프리미엄'
  AND NOT EXISTS (SELECT 1 FROM PLANS P WHERE P.PLANCODE = 'Premium');

INSERT INTO PLANS (PLANNAMEID, PLANCODE, PLANACTIVE, PLANCREATE)
SELECT PN.PLANNAMEID, 'Family', TRUE, CURRENT_TIMESTAMP
FROM PLANNAME PN
WHERE PN.PLANNAME = '패밀리'
  AND NOT EXISTS (SELECT 1 FROM PLANS P WHERE P.PLANCODE = 'Family');

INSERT INTO PLANS (PLANNAMEID, PLANCODE, PLANACTIVE, PLANCREATE)
SELECT PN.PLANNAMEID, 'VIP', TRUE, CURRENT_TIMESTAMP
FROM PLANNAME PN
WHERE PN.PLANNAME = 'VIP'
  AND NOT EXISTS (SELECT 1 FROM PLANS P WHERE P.PLANCODE = 'VIP');


/* 3) 플랜 혜택(월 대여료 상한) — pbNote NOT NULL 충족 */
INSERT INTO planBenefit (planId, pbPriceCap, pbNote)
SELECT p.planId, 75000.00, '베이직 플랜은 부담 없이 시작할 수 있는 입문형 구독!
- 월 75,000원 대여 한도 제공
- 무료 왕복 배송 월 1회
- 일반 추첨권 월 1장 제공'
FROM plans p
WHERE p.planCode='Basic'
  AND NOT EXISTS (SELECT 1 FROM planBenefit b WHERE b.planId = p.planId);

INSERT INTO planBenefit (planId, pbPriceCap, pbNote)
SELECT p.planId, 150000.00, '베이직 플랜에 5,000원만 추가하면 대여 상한이 두배!
- 월 150,000원 대여 한도 제공
- 무료 왕복 배송 월 2회
- 일반 추첨권 월 2장 제공'
FROM plans p
WHERE p.planCode='Standard'
  AND NOT EXISTS (SELECT 1 FROM planBenefit b WHERE b.planId = p.planId);

INSERT INTO planBenefit (planId, pbPriceCap, pbNote)
SELECT p.planId, 250000.00, '월 3만원이 안되는 금액으로 누릴 수 있는 최대혜택!
- 월 250,000원 대여 한도 제공
- 무료 왕복 배송 월 3회
- 일반 추첨권 월 3장 제공'
FROM plans p
WHERE p.planCode='Premium'
  AND NOT EXISTS (SELECT 1 FROM planBenefit b WHERE b.planId = p.planId);

INSERT INTO planBenefit (planId, pbPriceCap, pbNote)
SELECT p.planId, 1150000.00, '월 10만원이 안되는 가격에 혜택은 최대로!
- 월 1,150,000원 대여 한도 제공
- 무료 왕복 배송 무제한
- 1,000,000원 이상 제품 대여 가능
- 상품별 대여 금액 할인
- 연차별 누적 금액 혜택(이벤트 페이지 참고)
- 일반 추첨권 월 5장 제공
- 프리미엄 추첨권 월 1장 제공'
FROM plans p
WHERE p.planCode='Family'
  AND NOT EXISTS (SELECT 1 FROM planBenefit b WHERE b.planId = p.planId);

INSERT INTO planBenefit (planId, pbPriceCap, pbNote)
SELECT p.planId, 2000000.00, '이 플랜을 사용하는 고객님 도담도담의 VIP이십니다!
- 월 2,000,000원 대여 한도 제공
- 무료 왕복 배송 무제한
- 1,000,000원 이상 제품 대여 가능
- 상품별 대여 금액 할인
- 연차별 누적 금액 혜택(이벤트 페이지 참고)
- 일반 추첨권 월 7장 제공
- 프리미엄 추첨권 월 2장 제공'
FROM plans p
WHERE p.planCode='VIP'
  AND NOT EXISTS (SELECT 1 FROM planBenefit b WHERE b.planId = p.planId);

/* 4) 플랜 기간 옵션(1/3/6/12개월) */
MERGE INTO planTerms (ptermMonth)
KEY (ptermMonth)
VALUES (1), (3), (6), (12);

/* 5) 플랜 가격
   - 월요금: BASIC 14,900 / STANDARD 19,900 / PREMIUM 25,900 / FAMILY 89,000 / VIP 149,000
   - MONTHLY: 1개월만
   - PREPAID_TERM: 3/6/12개월 총액 = 월요금 × 개월수 (할인 미적용, 원한다면 수식만 바꾸면 됨)
*/

/* MONTHLY (1개월) */
INSERT INTO planPrice (planId, ptermId, ppriceBilMode, ppriceAmount, ppriceCurr, ppriceActive, ppriceCreate)
SELECT p.planId, t.ptermId, 'MONTHLY', 14900.00, 'KRW', TRUE, CURRENT_TIMESTAMP
FROM plans p
JOIN planTerms t ON t.ptermMonth = 1
WHERE p.planCode = 'Basic'
  AND NOT EXISTS (SELECT 1 FROM planPrice x
                  WHERE x.planId = p.planId AND x.ptermId = t.ptermId AND x.ppriceBilMode = 'MONTHLY');

INSERT INTO planPrice (planId, ptermId, ppriceBilMode, ppriceAmount, ppriceCurr, ppriceActive, ppriceCreate)
SELECT p.planId, t.ptermId, 'MONTHLY', 19900.00, 'KRW', TRUE, CURRENT_TIMESTAMP
FROM plans p
JOIN planTerms t ON t.ptermMonth = 1
WHERE p.planCode = 'Standard'
  AND NOT EXISTS (SELECT 1 FROM planPrice x
                  WHERE x.planId = p.planId AND x.ptermId = t.ptermId AND x.ppriceBilMode = 'MONTHLY');

INSERT INTO planPrice (planId, ptermId, ppriceBilMode, ppriceAmount, ppriceCurr, ppriceActive, ppriceCreate)
SELECT p.planId, t.ptermId, 'MONTHLY', 25900.00, 'KRW', TRUE, CURRENT_TIMESTAMP
FROM plans p
JOIN planTerms t ON t.ptermMonth = 1
WHERE p.planCode = 'Premium'
  AND NOT EXISTS (SELECT 1 FROM planPrice x
                  WHERE x.planId = p.planId AND x.ptermId = t.ptermId AND x.ppriceBilMode = 'MONTHLY');

INSERT INTO planPrice (planId, ptermId, ppriceBilMode, ppriceAmount, ppriceCurr, ppriceActive, ppriceCreate)
SELECT p.planId, t.ptermId, 'MONTHLY', 89000.00, 'KRW', TRUE, CURRENT_TIMESTAMP
FROM plans p
JOIN planTerms t ON t.ptermMonth = 1
WHERE p.planCode = 'Family'
  AND NOT EXISTS (SELECT 1 FROM planPrice x
                  WHERE x.planId = p.planId AND x.ptermId = t.ptermId AND x.ppriceBilMode = 'MONTHLY');

INSERT INTO planPrice (planId, ptermId, ppriceBilMode, ppriceAmount, ppriceCurr, ppriceActive, ppriceCreate)
SELECT p.planId, t.ptermId, 'MONTHLY', 149000.00, 'KRW', TRUE, CURRENT_TIMESTAMP
FROM plans p
JOIN planTerms t ON t.ptermMonth = 1
WHERE p.planCode = 'VIP'
  AND NOT EXISTS (SELECT 1 FROM planPrice x
                  WHERE x.planId = p.planId AND x.ptermId = t.ptermId AND x.ppriceBilMode = 'MONTHLY');

/* PREPAID_TERM (3/6/12개월 총액) */
INSERT INTO planPrice (planId, ptermId, ppriceBilMode, ppriceAmount, ppriceCurr, ppriceActive, ppriceCreate)
SELECT p.planId, t.ptermId, 'PREPAID_TERM', 14900.00 * t.ptermMonth, 'KRW', TRUE, CURRENT_TIMESTAMP
FROM plans p
JOIN planTerms t ON t.ptermMonth IN (3,6,12)
WHERE p.planCode = 'Basic'
  AND NOT EXISTS (SELECT 1 FROM planPrice x
                  WHERE x.planId = p.planId AND x.ptermId = t.ptermId AND x.ppriceBilMode = 'PREPAID_TERM');

INSERT INTO planPrice (planId, ptermId, ppriceBilMode, ppriceAmount, ppriceCurr, ppriceActive, ppriceCreate)
SELECT p.planId, t.ptermId, 'PREPAID_TERM', 19900.00 * t.ptermMonth, 'KRW', TRUE, CURRENT_TIMESTAMP
FROM plans p
JOIN planTerms t ON t.ptermMonth IN (3,6,12)
WHERE p.planCode = 'Standard'
  AND NOT EXISTS (SELECT 1 FROM planPrice x
                  WHERE x.planId = p.planId AND x.ptermId = t.ptermId AND x.ppriceBilMode = 'PREPAID_TERM');

INSERT INTO planPrice (planId, ptermId, ppriceBilMode, ppriceAmount, ppriceCurr, ppriceActive, ppriceCreate)
SELECT p.planId, t.ptermId, 'PREPAID_TERM', 25900.00 * t.ptermMonth, 'KRW', TRUE, CURRENT_TIMESTAMP
FROM plans p
JOIN planTerms t ON t.ptermMonth IN (3,6,12)
WHERE p.planCode = 'Premium'
  AND NOT EXISTS (SELECT 1 FROM planPrice x
                  WHERE x.planId = p.planId AND x.ptermId = t.ptermId AND x.ppriceBilMode = 'PREPAID_TERM');

INSERT INTO planPrice (planId, ptermId, ppriceBilMode, ppriceAmount, ppriceCurr, ppriceActive, ppriceCreate)
SELECT p.planId, t.ptermId, 'PREPAID_TERM', 89000.00 * t.ptermMonth, 'KRW', TRUE, CURRENT_TIMESTAMP
FROM plans p
JOIN planTerms t ON t.ptermMonth IN (3,6,12)
WHERE p.planCode = 'Family'
  AND NOT EXISTS (SELECT 1 FROM planPrice x
                  WHERE x.planId = p.planId AND x.ptermId = t.ptermId AND x.ppriceBilMode = 'PREPAID_TERM');

INSERT INTO planPrice (planId, ptermId, ppriceBilMode, ppriceAmount, ppriceCurr, ppriceActive, ppriceCreate)
SELECT p.planId, t.ptermId, 'PREPAID_TERM', 149000.00 * t.ptermMonth, 'KRW', TRUE, CURRENT_TIMESTAMP
FROM plans p
JOIN planTerms t ON t.ptermMonth IN (3,6,12)
WHERE p.planCode = 'VIP'
  AND NOT EXISTS (SELECT 1 FROM planPrice x
                  WHERE x.planId = p.planId AND x.ptermId = t.ptermId AND x.ppriceBilMode = 'PREPAID_TERM');
                  
UPDATE planBenefit
SET pbNote = '베이직 플랜은 부담 없이 시작할 수 있는 입문형 구독!
- 월 75,000원 대여 한도 제공
- 무료 왕복 배송 월 1회
- 일반 추첨권 월 1장 제공'
WHERE planId = 1;
UPDATE planBenefit
SET pbNote = '베이직 플랜에 5,000원만 추가하면 대여 상한이 두배!
- 월 150,000원 대여 한도 제공
- 무료 왕복 배송 월 2회
- 일반 추첨권 월 2장 제공'
WHERE planId = 2;
UPDATE planBenefit
SET pbNote = '월 3만원이 안되는 금액으로 누릴 수 있는 최대혜택!
- 월 250,000원 대여 한도 제공
- 무료 왕복 배송 월 3회
- 일반 추첨권 월 3장 제공'
WHERE planId = 3;
UPDATE planBenefit
SET pbNote = '월 10만원이 안되는 가격에 혜택은 최대로!
- 월 1,150,000원 대여 한도 제공
- 무료 왕복 배송 무제한
- 1,000,000원 이상 제품 대여 가능
- 상품별 대여 금액 할인
- 연차별 누적 금액 혜택(이벤트 페이지 참고)
- 일반 추첨권 월 5장 제공
- 프리미엄 추첨권 월 1장 제공'
WHERE planId = 4;
UPDATE planBenefit
SET pbNote = '이 플랜을 사용하는 고객님 도담도담의 VIP이십니다!
- 월 2,000,000원 대여 한도 제공
- 무료 왕복 배송 무제한
- 1,000,000원 이상 제품 대여 가능
- 상품별 대여 금액 할인
- 연차별 누적 금액 혜택(이벤트 페이지 참고)
- 일반 추첨권 월 7장 제공
- 프리미엄 추첨권 월 2장 제공'
WHERE planId = 5;