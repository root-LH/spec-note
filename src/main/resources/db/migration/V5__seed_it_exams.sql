-- =========================================================
-- 개발자 취준생이 많이 찾는 IT 자격 (Q-Net 종목 목록에 없어 수동 관리)
-- 회차 일정은 각 기관 공지를 확인해 관리자 화면에서 등록
-- =========================================================

-- 한국데이터산업진흥원 (SQLD는 V3에서 등록됨)
INSERT INTO exam (exam_code, name, category, organizer, validity_months, official_url, source)
VALUES (NULL, 'SQLP', 'PRIVATE', '한국데이터산업진흥원', NULL, 'https://www.dataq.or.kr', 'MANUAL');

INSERT INTO exam (exam_code, name, category, organizer, validity_months, official_url, source)
VALUES (NULL, 'ADsP', 'PRIVATE', '한국데이터산업진흥원', NULL, 'https://www.dataq.or.kr', 'MANUAL');

INSERT INTO exam (exam_code, name, category, organizer, validity_months, official_url, source)
VALUES (NULL, 'ADP', 'PRIVATE', '한국데이터산업진흥원', NULL, 'https://www.dataq.or.kr', 'MANUAL');

-- 국가기술자격이지만 Q-Net이 아닌 기관이 시행
INSERT INTO exam (exam_code, name, category, organizer, validity_months, official_url, source)
VALUES (NULL, '빅데이터분석기사', 'NATIONAL', '한국데이터산업진흥원', NULL, 'https://www.dataq.or.kr', 'MANUAL');

INSERT INTO exam (exam_code, name, category, organizer, validity_months, official_url, source)
VALUES (NULL, '정보보안기사', 'NATIONAL', '한국인터넷진흥원', NULL, 'https://www.kisq.or.kr', 'MANUAL');

-- 국가공인 민간자격
INSERT INTO exam (exam_code, name, category, organizer, validity_months, official_url, source)
VALUES (NULL, '리눅스마스터 1급', 'PRIVATE', '한국정보통신진흥협회', NULL, 'https://www.ihd.or.kr', 'MANUAL');

INSERT INTO exam (exam_code, name, category, organizer, validity_months, official_url, source)
VALUES (NULL, '리눅스마스터 2급', 'PRIVATE', '한국정보통신진흥협회', NULL, 'https://www.ihd.or.kr', 'MANUAL');

INSERT INTO exam (exam_code, name, category, organizer, validity_months, official_url, source)
VALUES (NULL, '네트워크관리사 2급', 'PRIVATE', '한국정보통신자격협회', NULL, 'https://www.icqa.or.kr', 'MANUAL');

-- 클라우드 (정해진 회차 없이 상시 응시 → 일정 없이 스펙 관리용, 유효기간 3년)
INSERT INTO exam (exam_code, name, category, organizer, validity_months, official_url, source)
VALUES (NULL, 'AWS Certified Solutions Architect - Associate', 'PRIVATE', 'Amazon Web Services', 36,
        'https://aws.amazon.com/certification/', 'MANUAL');
