package io.github.root_LH.specnote.exam.repository;

import io.github.root_LH.specnote.exam.domain.ExamCategory;
import io.github.root_LH.specnote.exam.domain.ExamSchedule;
import io.github.root_LH.specnote.exam.domain.ExamStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Long> {

    /**
     * 아직 끝나지 않은 일정 (발표일 → 시험일 → 접수 마감 중 있는 값이 오늘 이후)
     */
    @Query("""
            select s from ExamSchedule s
            where s.exam.id = :examId
              and coalesce(s.resultDate, s.examDate, s.regEnd) >= :today
            order by coalesce(s.regStart, s.examDate) asc
            """)
    List<ExamSchedule> findUpcomingByExamId(@Param("examId") Long examId,
                                            @Param("today") LocalDate today);

    /**
     * 캘린더용: 접수 시작·마감·시험일·발표일 중 하나라도 기간 안에 있는 일정.
     * join fetch로 시험 정보를 한 번에 가져와 N+1 문제를 방지.
     */
    @Query("""
            select s from ExamSchedule s
            join fetch s.exam e
            where (:category is null or e.category = :category)
              and (s.regStart   between :from and :to
                or s.regEnd     between :from and :to
                or s.examDate   between :from and :to
                or s.resultDate between :from and :to)
            order by coalesce(s.regStart, s.examDate) asc
            """)
    List<ExamSchedule> findInPeriod(@Param("from") LocalDate from,
                                    @Param("to") LocalDate to,
                                    @Param("category") ExamCategory category);

    boolean existsByExamIdAndRoundNameAndStage(Long examId, String roundName, ExamStage stage);
}
