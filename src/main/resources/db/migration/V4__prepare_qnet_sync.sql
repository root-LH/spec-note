-- 공공데이터 동기화 대상(exam_code가 있는 국가자격)의 샘플 일정을 삭제.
-- 실제 일정이 동기화되면 샘플과 섞여 보이지 않도록 함.
-- (Oracle의 LIKE에서 대괄호는 일반 문자로 취급됨)
DELETE FROM exam_schedule
WHERE round_name LIKE '[샘플]%'
  AND exam_id IN (SELECT exam_id FROM exam WHERE exam_code IS NOT NULL);

-- 동기화로 관리되는 시험은 출처를 API로 표시
UPDATE exam SET source = 'API' WHERE exam_code IS NOT NULL;
