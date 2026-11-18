-- Oracle VARCHAR2(30)은 기본이 "30바이트"라서 한글(1글자 = 3바이트)은 10자까지만 들어감.
-- 한글이 들어가는 컬럼은 "글자 수" 기준(CHAR)으로 변경.
ALTER TABLE member        MODIFY (nickname    VARCHAR2(30 CHAR));
ALTER TABLE exam          MODIFY (name        VARCHAR2(100 CHAR), organizer VARCHAR2(100 CHAR));
ALTER TABLE exam_schedule MODIFY (round_name  VARCHAR2(50 CHAR));
ALTER TABLE member_spec   MODIFY (custom_name VARCHAR2(100 CHAR), score VARCHAR2(20 CHAR));
