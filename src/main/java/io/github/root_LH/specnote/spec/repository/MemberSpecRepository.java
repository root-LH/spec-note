package io.github.root_LH.specnote.spec.repository;

import io.github.root_LH.specnote.spec.domain.MemberSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MemberSpecRepository extends JpaRepository<MemberSpec, Long> {

    /**
     * 내 스펙 목록. exam이 없는 스펙도 있으므로 left join fetch
     */
    @Query("""
            select s from MemberSpec s
            left join fetch s.exam
            where s.member.id = :memberId
            order by s.acquiredDate desc
            """)
    List<MemberSpec> findAllWithExamByMemberId(@Param("memberId") Long memberId);

    /**
     * 본인 스펙만 조회 (다른 회원 스펙 ID로 접근하면 "없음"으로 처리)
     */
    Optional<MemberSpec> findByIdAndMemberId(Long id, Long memberId);

    // ===== 만료 알림 배치용 =====

    @Query("""
            select s from MemberSpec s
            join fetch s.member
            left join fetch s.exam
            where s.expireDate = :date
            """)
    List<MemberSpec> findWithMemberAndExamByExpireDate(@Param("date") LocalDate date);

    @Query("""
            select s from MemberSpec s
            join fetch s.member
            left join fetch s.exam
            where s.id = :id
            """)
    Optional<MemberSpec> findWithMemberAndExamById(@Param("id") Long id);
}
