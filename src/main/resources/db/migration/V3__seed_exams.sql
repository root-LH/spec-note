-- =========================================================
-- 기본 시험 목록
-- 국가자격 일정은 이후 공공데이터 API 동기화로 채울 예정 (exam_code로 매칭)
-- =========================================================
INSERT INTO exam (exam_code, name, category, organizer, validity_months, official_url, source)
VALUES ('1320', '정보처리기사', 'NATIONAL', '한국산업인력공단', NULL, 'https://www.q-net.or.kr', 'MANUAL');

INSERT INTO exam (exam_code, name, category, organizer, validity_months, official_url, source)
VALUES (NULL, '컴퓨터활용능력 1급', 'NATIONAL', '대한상공회의소', NULL, 'https://license.korcham.net', 'MANUAL');

INSERT INTO exam (exam_code, name, category, organizer, validity_months, official_url, source)
VALUES (NULL, '한국사능력검정시험', 'NATIONAL', '국사편찬위원회', NULL, 'https://www.historyexam.go.kr', 'MANUAL');

INSERT INTO exam (exam_code, name, category, organizer, validity_months, official_url, source)
VALUES (NULL, 'SQLD', 'PRIVATE', '한국데이터산업진흥원', NULL, 'https://www.dataq.or.kr', 'MANUAL');

INSERT INTO exam (exam_code, name, category, organizer, validity_months, official_url, source)
VALUES (NULL, 'TOEIC', 'LANGUAGE', 'YBM 한국TOEIC위원회', 24, 'https://exam.toeic.co.kr', 'MANUAL');

INSERT INTO exam (exam_code, name, category, organizer, validity_months, official_url, source)
VALUES (NULL, 'TOEIC Speaking', 'LANGUAGE', 'YBM 한국TOEIC위원회', 24, 'https://exam.toeic.co.kr', 'MANUAL');

INSERT INTO exam (exam_code, name, category, organizer, validity_months, official_url, source)
VALUES (NULL, 'OPIc', 'LANGUAGE', 'ACTFL / 멀티캠퍼스', 24, 'https://www.opic.or.kr', 'MANUAL');

INSERT INTO exam (exam_code, name, category, organizer, validity_months, official_url, source)
VALUES (NULL, 'JLPT', 'LANGUAGE', '일본국제교류기금', NULL, 'https://www.jlpt.or.kr', 'MANUAL');

-- =========================================================
-- ⚠️ 샘플 일정 (실제 일정 아님, 화면·API 테스트용)
-- 접수 예정 / 접수 중 / 접수 마감 상태가 골고루 나오도록 2026년 11월 기준으로 구성
-- =========================================================
INSERT INTO exam_schedule (exam_id, round_name, stage, reg_start, reg_end, exam_date, result_date)
VALUES ((SELECT exam_id FROM exam WHERE name = 'TOEIC'),
        '[샘플] 1회차', 'SINGLE', DATE '2026-11-10', DATE '2026-11-27', DATE '2026-12-13', DATE '2026-12-24');

INSERT INTO exam_schedule (exam_id, round_name, stage, reg_start, reg_end, exam_date, result_date)
VALUES ((SELECT exam_id FROM exam WHERE name = 'TOEIC'),
        '[샘플] 2회차', 'SINGLE', DATE '2026-12-01', DATE '2026-12-25', DATE '2027-01-10', DATE '2027-01-21');

INSERT INTO exam_schedule (exam_id, round_name, stage, reg_start, reg_end, exam_date, result_date)
VALUES ((SELECT exam_id FROM exam WHERE name = 'SQLD'),
        '[샘플] 1회차', 'SINGLE', DATE '2026-10-20', DATE '2026-10-24', DATE '2026-11-22', DATE '2026-12-11');

INSERT INTO exam_schedule (exam_id, round_name, stage, reg_start, reg_end, exam_date, result_date)
VALUES ((SELECT exam_id FROM exam WHERE name = '한국사능력검정시험'),
        '[샘플] 1회차', 'SINGLE', DATE '2026-12-08', DATE '2026-12-15', DATE '2027-01-17', DATE '2027-01-30');

INSERT INTO exam_schedule (exam_id, round_name, stage, reg_start, reg_end, exam_date, result_date)
VALUES ((SELECT exam_id FROM exam WHERE name = '정보처리기사'),
        '[샘플] 2027년 1회', 'WRITTEN', DATE '2027-01-12', DATE '2027-01-15', DATE '2027-02-14', DATE '2027-03-12');

INSERT INTO exam_schedule (exam_id, round_name, stage, reg_start, reg_end, exam_date, result_date)
VALUES ((SELECT exam_id FROM exam WHERE name = '정보처리기사'),
        '[샘플] 2027년 1회', 'PRACTICAL', DATE '2027-03-24', DATE '2027-03-27', DATE '2027-04-19', DATE '2027-06-18');
