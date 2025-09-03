-- PlanData.sql (CamelCase only, H2 2.x)
-- 작성: 2025-09-03 KST
-- 전제: 테이블명이 CamelCase (planName / plans / planBenefit / planTerms / planPrice)
-- 주의: IDENTITY/PK 컬럼은 DB가 자동 생성 → MERGE 시 ID 컬럼을 명시하지 않음

/* 1) 플랜 이름(Basic/Standard/Premium/Family/VIP) */
MERGE INTO planName (planName)
KEY (planName)
VALUES ('Basic'),
       ('Standard'),
       ('Premium'),
       ('Family'),
       ('VIP');

/* 2) 플랜 마스터: planCode = BASIC/STANDARD/PREMIUM/FAMILY/VIP */
INSERT INTO plans (planNameId, planCode, planActive, planCreate)
SELECT pn.planNameId, 'BASIC', TRUE, CURRENT_TIMESTAMP
FROM planName pn
WHERE pn.planName = 'Basic'
  AND NOT EXISTS (SELECT 1 FROM plans p WHERE p.planCode = 'BASIC');

INSERT INTO plans (planNameId, planCode, planActive, planCreate)
SELECT pn.planNameId, 'STANDARD', TRUE, CURRENT_TIMESTAMP
FROM planName pn
WHERE pn.planName = 'Standard'
  AND NOT EXISTS (SELECT 1 FROM plans p WHERE p.planCode = 'STANDARD');

INSERT INTO plans (planNameId, planCode, planActive, planCreate)
SELECT pn.planNameId, 'PREMIUM', TRUE, CURRENT_TIMESTAMP
FROM planName pn
WHERE pn.planName = 'Premium'
  AND NOT EXISTS (SELECT 1 FROM plans p WHERE p.planCode = 'PREMIUM');

INSERT INTO plans (planNameId, planCode, planActive, planCreate)
SELECT pn.planNameId, 'FAMILY', TRUE, CURRENT_TIMESTAMP
FROM planName pn
WHERE pn.planName = 'Family'
  AND NOT EXISTS (SELECT 1 FROM plans p WHERE p.planCode = 'FAMILY');

INSERT INTO plans (planNameId, planCode, planActive, planCreate)
SELECT pn.planNameId, 'VIP', TRUE, CURRENT_TIMESTAMP
FROM planName pn
WHERE pn.planName = 'VIP'
  AND NOT EXISTS (SELECT 1 FROM plans p WHERE p.planCode = 'VIP');

/* 3) 플랜 혜택(월 대여료 상한) — pbNote NOT NULL 충족 */
INSERT INTO planBenefit (planId, pbPriceCap, pbNote)
SELECT p.planId, 75000.00, '월 대여료 상한: 75,000 KRW'
FROM plans p
WHERE p.planCode='BASIC'
  AND NOT EXISTS (SELECT 1 FROM planBenefit b WHERE b.planId = p.planId);

INSERT INTO planBenefit (planId, pbPriceCap, pbNote)
SELECT p.planId, 150000.00, '월 대여료 상한: 150,000 KRW'
FROM plans p
WHERE p.planCode='STANDARD'
  AND NOT EXISTS (SELECT 1 FROM planBenefit b WHERE b.planId = p.planId);

INSERT INTO planBenefit (planId, pbPriceCap, pbNote)
SELECT p.planId, 250000.00, '월 대여료 상한: 250,000 KRW'
FROM plans p
WHERE p.planCode='PREMIUM'
  AND NOT EXISTS (SELECT 1 FROM planBenefit b WHERE b.planId = p.planId);

INSERT INTO planBenefit (planId, pbPriceCap, pbNote)
SELECT p.planId, 1150000.00, '월 대여료 상한: 1,150,000 KRW'
FROM plans p
WHERE p.planCode='FAMILY'
  AND NOT EXISTS (SELECT 1 FROM planBenefit b WHERE b.planId = p.planId);

INSERT INTO planBenefit (planId, pbPriceCap, pbNote)
SELECT p.planId, 2000000.00, '월 대여료 상한: 2,000,000 KRW'
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
WHERE p.planCode = 'BASIC'
  AND NOT EXISTS (SELECT 1 FROM planPrice x
                  WHERE x.planId = p.planId AND x.ptermId = t.ptermId AND x.ppriceBilMode = 'MONTHLY');

INSERT INTO planPrice (planId, ptermId, ppriceBilMode, ppriceAmount, ppriceCurr, ppriceActive, ppriceCreate)
SELECT p.planId, t.ptermId, 'MONTHLY', 19900.00, 'KRW', TRUE, CURRENT_TIMESTAMP
FROM plans p
JOIN planTerms t ON t.ptermMonth = 1
WHERE p.planCode = 'STANDARD'
  AND NOT EXISTS (SELECT 1 FROM planPrice x
                  WHERE x.planId = p.planId AND x.ptermId = t.ptermId AND x.ppriceBilMode = 'MONTHLY');

INSERT INTO planPrice (planId, ptermId, ppriceBilMode, ppriceAmount, ppriceCurr, ppriceActive, ppriceCreate)
SELECT p.planId, t.ptermId, 'MONTHLY', 25900.00, 'KRW', TRUE, CURRENT_TIMESTAMP
FROM plans p
JOIN planTerms t ON t.ptermMonth = 1
WHERE p.planCode = 'PREMIUM'
  AND NOT EXISTS (SELECT 1 FROM planPrice x
                  WHERE x.planId = p.planId AND x.ptermId = t.ptermId AND x.ppriceBilMode = 'MONTHLY');

INSERT INTO planPrice (planId, ptermId, ppriceBilMode, ppriceAmount, ppriceCurr, ppriceActive, ppriceCreate)
SELECT p.planId, t.ptermId, 'MONTHLY', 89000.00, 'KRW', TRUE, CURRENT_TIMESTAMP
FROM plans p
JOIN planTerms t ON t.ptermMonth = 1
WHERE p.planCode = 'FAMILY'
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
WHERE p.planCode = 'BASIC'
  AND NOT EXISTS (SELECT 1 FROM planPrice x
                  WHERE x.planId = p.planId AND x.ptermId = t.ptermId AND x.ppriceBilMode = 'PREPAID_TERM');

INSERT INTO planPrice (planId, ptermId, ppriceBilMode, ppriceAmount, ppriceCurr, ppriceActive, ppriceCreate)
SELECT p.planId, t.ptermId, 'PREPAID_TERM', 19900.00 * t.ptermMonth, 'KRW', TRUE, CURRENT_TIMESTAMP
FROM plans p
JOIN planTerms t ON t.ptermMonth IN (3,6,12)
WHERE p.planCode = 'STANDARD'
  AND NOT EXISTS (SELECT 1 FROM planPrice x
                  WHERE x.planId = p.planId AND x.ptermId = t.ptermId AND x.ppriceBilMode = 'PREPAID_TERM');

INSERT INTO planPrice (planId, ptermId, ppriceBilMode, ppriceAmount, ppriceCurr, ppriceActive, ppriceCreate)
SELECT p.planId, t.ptermId, 'PREPAID_TERM', 25900.00 * t.ptermMonth, 'KRW', TRUE, CURRENT_TIMESTAMP
FROM plans p
JOIN planTerms t ON t.ptermMonth IN (3,6,12)
WHERE p.planCode = 'PREMIUM'
  AND NOT EXISTS (SELECT 1 FROM planPrice x
                  WHERE x.planId = p.planId AND x.ptermId = t.ptermId AND x.ppriceBilMode = 'PREPAID_TERM');

INSERT INTO planPrice (planId, ptermId, ppriceBilMode, ppriceAmount, ppriceCurr, ppriceActive, ppriceCreate)
SELECT p.planId, t.ptermId, 'PREPAID_TERM', 89000.00 * t.ptermMonth, 'KRW', TRUE, CURRENT_TIMESTAMP
FROM plans p
JOIN planTerms t ON t.ptermMonth IN (3,6,12)
WHERE p.planCode = 'FAMILY'
  AND NOT EXISTS (SELECT 1 FROM planPrice x
                  WHERE x.planId = p.planId AND x.ptermId = t.ptermId AND x.ppriceBilMode = 'PREPAID_TERM');

INSERT INTO planPrice (planId, ptermId, ppriceBilMode, ppriceAmount, ppriceCurr, ppriceActive, ppriceCreate)
SELECT p.planId, t.ptermId, 'PREPAID_TERM', 149000.00 * t.ptermMonth, 'KRW', TRUE, CURRENT_TIMESTAMP
FROM plans p
JOIN planTerms t ON t.ptermMonth IN (3,6,12)
WHERE p.planCode = 'VIP'
  AND NOT EXISTS (SELECT 1 FROM planPrice x
                  WHERE x.planId = p.planId AND x.ptermId = t.ptermId AND x.ppriceBilMode = 'PREPAID_TERM');
