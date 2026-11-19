package io.github.root_LH.specnote.exam.repository;

import io.github.root_LH.specnote.exam.domain.ExamCategory;
import io.github.root_LH.specnote.exam.domain.ExamSchedule;
import io.github.root_LH.specnote.exam.domain.ExamStage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
     * 대시보드용: 내가 구독한 시험들의 아직 끝나지 않은 일정 (개수는 pageable로 제한)
     */
    @Query("""
            select s from ExamSchedule s
            join fetch s.exam e
            where e.id in (select sub.exam.id from Subscription sub where sub.member.id = :memberId)
              and coalesce(s.resultDate, s.examDate, s.regEnd) >= :today
            order by coalesce(s.regStart, s.examDate) asc
            """)
    List<ExamSchedule> findUpcomingForSubscriber(@Param("memberId") Long memberId,
                                                 @Param("today") LocalDate today,
                                                 Pageable pageable);

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

    // ===== 알림 배치용: 특정 날짜에 해당하는 일정 (시험 정보까지 한 번에) =====

    @Query("select s from ExamSchedule s join fetch s.exam where s.regStart = :date")
    List<ExamSchedule> findWithExamByRegStart(@Param("date") LocalDate date);

    @Query("select s from ExamSchedule s join fetch s.exam where s.regEnd = :date")
    List<ExamSchedule> findWithExamByRegEnd(@Param("date") LocalDate date);

    @Query("select s from ExamSchedule s join fetch s.exam where s.examDate = :date")
    List<ExamSchedule> findWithExamByExamDate(@Param("date") LocalDate date);

    @Query("select s from ExamSchedule s join fetch s.exam where s.resultDate = :date")
    List<ExamSchedule> findWithExamByResultDate(@Param("date") LocalDate date);

    @Query("select s from ExamSchedule s join fetch s.exam where s.id = :id")
    Optional<ExamSchedule> findWithExamById(@Param("id") Long id);
}
