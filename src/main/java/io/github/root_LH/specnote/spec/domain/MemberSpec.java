package io.github.root_LH.specnote.spec.domain;

import io.github.root_LH.specnote.exam.domain.Exam;
import io.github.root_LH.specnote.global.exception.BusinessException;
import io.github.root_LH.specnote.global.exception.ErrorCode;
import io.github.root_LH.specnote.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 회원이 보유한 자격증·어학 점수.
 * 목록에 있는 시험이면 exam을 연결하고, 없으면 customName에 직접 입력.
 */
@Entity
@Table(name = "member_spec")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MemberSpec {

    /** 만료일까지 이 일수 이하로 남으면 "만료 임박" */
    public static final int EXPIRING_SOON_DAYS = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "spec_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** 목록에 없는 스펙이면 null */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id")
    private Exam exam;

    @Column(name = "custom_name", length = 100)
    private String customName;

    @Column(length = 20)
    private String score;

    @Column(name = "cert_number", length = 50)
    private String certNumber;

    @Column(name = "acquired_date", nullable = false)
    private LocalDate acquiredDate;

    /** null이면 영구 */
    @Column(name = "expire_date")
    private LocalDate expireDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private MemberSpec(Member member) {
        this.member = member;
    }

    /**
     * @param expireDate 직접 입력한 만료일. 비우면 시험의 유효기간으로 자동 계산
     */
    public static MemberSpec create(Member member, Exam exam, String customName, String score,
                                    String certNumber, LocalDate acquiredDate, LocalDate expireDate) {
        MemberSpec spec = new MemberSpec(member);
        spec.apply(exam, customName, score, certNumber, acquiredDate, expireDate);
        return spec;
    }

    public void update(Exam exam, String customName, String score,
                       String certNumber, LocalDate acquiredDate, LocalDate expireDate) {
        apply(exam, customName, score, certNumber, acquiredDate, expireDate);
    }

    private void apply(Exam exam, String customName, String score,
                       String certNumber, LocalDate acquiredDate, LocalDate expireDate) {
        if (exam == null && !StringUtils.hasText(customName)) {
            throw new BusinessException(ErrorCode.SPEC_NAME_REQUIRED);
        }
        LocalDate resolvedExpireDate = expireDate != null ? expireDate : calculateExpireDate(exam, acquiredDate);
        if (resolvedExpireDate != null && resolvedExpireDate.isBefore(acquiredDate)) {
            throw new BusinessException(ErrorCode.INVALID_SPEC_DATES);
        }

        this.exam = exam;
        this.customName = exam == null ? customName.trim() : null;   // 시험을 연결했으면 이름은 시험 것을 사용
        this.score = score;
        this.certNumber = certNumber;
        this.acquiredDate = acquiredDate;
        this.expireDate = resolvedExpireDate;
    }

    /** 시험에 유효기간(개월)이 있으면 취득일 + 유효기간, 없으면 영구(null) */
    private static LocalDate calculateExpireDate(Exam exam, LocalDate acquiredDate) {
        if (exam == null || exam.getValidityMonths() == null) {
            return null;
        }
        return acquiredDate.plusMonths(exam.getValidityMonths());
    }

    /** 화면에 보여줄 이름 */
    public String displayName() {
        return exam != null ? exam.getName() : customName;
    }

    /** 만료일까지 남은 일수 (영구면 null, 지났으면 음수) */
    public Long daysUntilExpire(LocalDate today) {
        return expireDate == null ? null : ChronoUnit.DAYS.between(today, expireDate);
    }

    public SpecStatus status(LocalDate today) {
        if (expireDate == null) {
            return SpecStatus.PERMANENT;
        }
        long days = daysUntilExpire(today);
        if (days < 0) {
            return SpecStatus.EXPIRED;
        }
        if (days <= EXPIRING_SOON_DAYS) {
            return SpecStatus.EXPIRING_SOON;
        }
        return SpecStatus.ACTIVE;
    }
}
