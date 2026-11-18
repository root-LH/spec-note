package io.github.root_LH.specnote.subscription.repository;

import io.github.root_LH.specnote.subscription.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsByMemberIdAndExamId(Long memberId, Long examId);

    Optional<Subscription> findByMemberIdAndExamId(Long memberId, Long examId);

    /** 내 구독 목록 (시험 정보 포함) */
    @Query("""
            select s from Subscription s
            join fetch s.exam
            where s.member.id = :memberId
            order by s.createdAt desc
            """)
    List<Subscription> findAllWithExamByMemberId(@Param("memberId") Long memberId);

    /** 알림 대상: 이 시험을 구독한 회원들 (회원 정보 포함) */
    @Query("""
            select s from Subscription s
            join fetch s.member
            where s.exam.id = :examId
            """)
    List<Subscription> findAllWithMemberByExamId(@Param("examId") Long examId);
}
