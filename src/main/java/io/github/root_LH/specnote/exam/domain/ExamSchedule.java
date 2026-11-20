package io.github.root_LH.specnote.exam.domain;

import io.github.root_LH.specnote.global.exception.BusinessException;
import io.github.root_LH.specnote.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 시험 회차별 일정 (접수 기간, 시험일, 발표일)
 */
@Entity
@Table(name = "exam_schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExamSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long id;

    // LAZY: 일정만 필요할 때 시험 정보까지 매번 조회하지 않도록 함 (필요하면 fetch join)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(name = "round_name", nullable = false, length = 50)
    private String roundName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ExamStage stage;

    @Column(name = "reg_start")
    private LocalDate regStart;

    @Column(name = "reg_end")
    private LocalDate regEnd;

    @Column(name = "exam_date")
    private LocalDate examDate;

    @Column(name = "result_date")
    private LocalDate resultDate;

    /** 공공데이터 API로 마지막 동기화한 시각 (수동 등록이면 null) */
    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    private ExamSchedule(Exam exam, String roundName, ExamStage stage,
                         LocalDate regStart, LocalDate regEnd, LocalDate examDate, LocalDate resultDate) {
        this.exam = exam;
        this.roundName = roundName;
        this.stage = stage;
        this.regStart = regStart;
        this.regEnd = regEnd;
        this.examDate = examDate;
        this.resultDate = resultDate;
    }

    public static ExamSchedule create(Exam exam, String roundName, ExamStage stage,
                                      LocalDate regStart, LocalDate regEnd,
                                      LocalDate examDate, LocalDate resultDate) {
        validateDateOrder(regStart, regEnd, examDate, resultDate);
        return new ExamSchedule(exam, roundName, stage, regStart, regEnd, examDate, resultDate);
    }

    /** 동기화나 관리자 수정으로 날짜가 바뀔 때 */
    public void changeDates(LocalDate regStart, LocalDate regEnd, LocalDate examDate, LocalDate resultDate) {
        validateDateOrder(regStart, regEnd, examDate, resultDate);
        this.regStart = regStart;
        this.regEnd = regEnd;
        this.examDate = examDate;
        this.resultDate = resultDate;
    }

    /** 동기화할 때 바뀐 게 없으면 UPDATE를 하지 않기 위한 비교 */
    public boolean hasSameDates(LocalDate regStart, LocalDate regEnd, LocalDate examDate, LocalDate resultDate) {
        return Objects.equals(this.regStart, regStart)
                && Objects.equals(this.regEnd, regEnd)
                && Objects.equals(this.examDate, examDate)
                && Objects.equals(this.resultDate, resultDate);
    }

    public void markSynced(LocalDateTime syncedAt) {
        this.syncedAt = syncedAt;
    }

    /**
     * 오늘 기준 접수 상태를 계산
     */
    public RegistrationStatus registrationStatus(LocalDate today) {
        if (regStart == null || regEnd == null) {
            return RegistrationStatus.UNKNOWN;
        }
        if (today.isBefore(regStart)) {
            return RegistrationStatus.BEFORE;
        }
        if (today.isAfter(regEnd)) {
            return RegistrationStatus.CLOSED;
        }
        return RegistrationStatus.OPEN;
    }

    /**
     * 접수 시작 ≤ 접수 마감 ≤ 시험일 ≤ 발표일 순서 검증.
     * 값이 비어 있는 날짜(미정)는 비교에서 제외.
     */
    private static void validateDateOrder(LocalDate regStart, LocalDate regEnd,
                                          LocalDate examDate, LocalDate resultDate) {
        if (isAfter(regStart, regEnd) || isAfter(regEnd, examDate) || isAfter(examDate, resultDate)
                || isAfter(regStart, examDate) || isAfter(regEnd, resultDate)) {
            throw new BusinessException(ErrorCode.INVALID_SCHEDULE_DATES);
        }
    }

    private static boolean isAfter(LocalDate a, LocalDate b) {
        return a != null && b != null && a.isAfter(b);
    }
}
