package io.github.root_LH.specnote.spec.domain;

import io.github.root_LH.specnote.exam.domain.Exam;
import io.github.root_LH.specnote.exam.domain.ExamCategory;
import io.github.root_LH.specnote.global.exception.BusinessException;
import io.github.root_LH.specnote.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 만료일 계산과 상태 판단을 DB 없이 검증
 * (member는 이 로직과 관계없어서 null로 둠)
 */
class MemberSpecTest {

    private final Exam toeic = Exam.createManual(null, "TOEIC", ExamCategory.LANGUAGE, "YBM", 24, null);
    private final Exam engineer = Exam.createManual("1320", "정보처리기사", ExamCategory.NATIONAL, "공단", null, null);

    @Nested
    @DisplayName("만료일 계산")
    class ExpireDate {

        @Test
        void 유효기간이_있는_시험은_취득일에_개월수를_더한다() {
            MemberSpec spec = MemberSpec.create(null, toeic, null, "855", null,
                    LocalDate.of(2024, 12, 19), null);

            assertThat(spec.getExpireDate()).isEqualTo(LocalDate.of(2026, 12, 19));
        }

        @Test
        void 유효기간이_없는_시험은_영구() {
            MemberSpec spec = MemberSpec.create(null, engineer, null, null, null,
                    LocalDate.of(2024, 6, 1), null);

            assertThat(spec.getExpireDate()).isNull();
            assertThat(spec.status(LocalDate.of(2030, 1, 1))).isEqualTo(SpecStatus.PERMANENT);
        }

        @Test
        void 만료일을_직접_입력하면_자동_계산보다_우선한다() {
            MemberSpec spec = MemberSpec.create(null, toeic, null, "855", null,
                    LocalDate.of(2024, 12, 19), LocalDate.of(2025, 6, 30));

            assertThat(spec.getExpireDate()).isEqualTo(LocalDate.of(2025, 6, 30));
        }

        @Test
        void 만료일이_취득일보다_빠르면_예외() {
            assertThatThrownBy(() -> MemberSpec.create(null, toeic, null, null, null,
                    LocalDate.of(2024, 12, 19), LocalDate.of(2024, 1, 1)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.INVALID_SPEC_DATES);
        }
    }

    @Nested
    @DisplayName("이름")
    class Name {

        @Test
        void 시험도_이름도_없으면_예외() {
            assertThatThrownBy(() -> MemberSpec.create(null, null, "  ", null, null,
                    LocalDate.of(2024, 1, 1), null))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.SPEC_NAME_REQUIRED);
        }

        @Test
        void 목록에_없는_스펙은_직접_입력한_이름을_쓴다() {
            MemberSpec spec = MemberSpec.create(null, null, " 리눅스마스터 2급 ", null, null,
                    LocalDate.of(2024, 1, 1), null);

            assertThat(spec.displayName()).isEqualTo("리눅스마스터 2급");
        }
    }

    @Nested
    @DisplayName("상태")
    class Status {

        // 만료일 2026-12-19
        private final MemberSpec spec = MemberSpec.create(null, toeic, null, "855", null,
                LocalDate.of(2024, 12, 19), null);

        @Test
        void 만료_31일_전은_유효() {
            assertThat(spec.status(LocalDate.of(2026, 11, 18))).isEqualTo(SpecStatus.ACTIVE);
        }

        @Test
        void 만료_30일_전부터_만료_임박() {
            assertThat(spec.status(LocalDate.of(2026, 11, 19))).isEqualTo(SpecStatus.EXPIRING_SOON);
            assertThat(spec.daysUntilExpire(LocalDate.of(2026, 11, 19))).isEqualTo(30L);
        }

        @Test
        void 만료일_당일까지는_만료_임박() {
            assertThat(spec.status(LocalDate.of(2026, 12, 19))).isEqualTo(SpecStatus.EXPIRING_SOON);
        }

        @Test
        void 만료일_다음날부터_만료() {
            assertThat(spec.status(LocalDate.of(2026, 12, 20))).isEqualTo(SpecStatus.EXPIRED);
        }
    }
}
