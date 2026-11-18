package io.github.root_LH.specnote.exam.domain;

import io.github.root_LH.specnote.global.exception.BusinessException;
import io.github.root_LH.specnote.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DB나 스프링 없이 도메인 로직만 검증하는 단위 테스트 (빠르게 실행됨)
 */
class ExamScheduleTest {

    private final Exam toeic = Exam.createManual(null, "TOEIC", ExamCategory.LANGUAGE, "YBM", 24, null);

    private ExamSchedule schedule(String regStart, String regEnd, String examDate, String resultDate) {
        return ExamSchedule.create(toeic, "1회", ExamStage.SINGLE,
                date(regStart), date(regEnd), date(examDate), date(resultDate));
    }

    private static LocalDate date(String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    @Nested
    @DisplayName("일정 생성")
    class Create {

        @Test
        @DisplayName("날짜 순서가 올바르면 생성된다")
        void validOrder() {
            ExamSchedule schedule = schedule("2026-11-10", "2026-11-27", "2026-12-13", "2026-12-24");

            assertThat(schedule.getExamDate()).isEqualTo(LocalDate.of(2026, 12, 13));
        }

        @Test
        @DisplayName("미정인 날짜(null)가 있어도 나머지 순서가 맞으면 생성된다")
        void nullDatesAllowed() {
            ExamSchedule schedule = schedule(null, null, "2026-12-13", null);

            assertThat(schedule.getRegStart()).isNull();
        }

        @Test
        @DisplayName("접수 마감이 접수 시작보다 빠르면 예외")
        void regEndBeforeRegStart() {
            assertThatThrownBy(() -> schedule("2026-11-27", "2026-11-10", "2026-12-13", null))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.INVALID_SCHEDULE_DATES);
        }

        @Test
        @DisplayName("발표일이 시험일보다 빠르면 예외")
        void resultBeforeExam() {
            assertThatThrownBy(() -> schedule(null, null, "2026-12-13", "2026-12-01"))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("중간 날짜가 비어 있어도 접수 시작이 시험일보다 늦으면 예외")
        void regStartAfterExamWithGap() {
            assertThatThrownBy(() -> schedule("2026-12-20", null, "2026-12-13", null))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("접수 상태 계산")
    class Status {

        private final ExamSchedule schedule = schedule("2026-11-10", "2026-11-27", "2026-12-13", "2026-12-24");

        @Test
        void 접수_시작_전이면_BEFORE() {
            assertThat(schedule.registrationStatus(LocalDate.of(2026, 11, 9)))
                    .isEqualTo(RegistrationStatus.BEFORE);
        }

        @Test
        void 접수_시작일과_마감일_당일은_OPEN() {
            assertThat(schedule.registrationStatus(LocalDate.of(2026, 11, 10))).isEqualTo(RegistrationStatus.OPEN);
            assertThat(schedule.registrationStatus(LocalDate.of(2026, 11, 27))).isEqualTo(RegistrationStatus.OPEN);
        }

        @Test
        void 접수_마감_다음날부터_CLOSED() {
            assertThat(schedule.registrationStatus(LocalDate.of(2026, 11, 28)))
                    .isEqualTo(RegistrationStatus.CLOSED);
        }

        @Test
        void 접수_기간이_미정이면_UNKNOWN() {
            ExamSchedule noReg = schedule(null, null, "2026-12-13", null);

            assertThat(noReg.registrationStatus(LocalDate.of(2026, 11, 20)))
                    .isEqualTo(RegistrationStatus.UNKNOWN);
        }
    }
}
