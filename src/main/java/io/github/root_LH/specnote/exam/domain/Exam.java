package io.github.root_LH.specnote.exam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 시험 종류 (정보처리기사, TOEIC …)
 */
@Entity
@Table(name = "exam")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exam_id")
    private Long id;

    /** 외부 시스템 코드 (Q-Net 종목코드 등). 공공데이터 동기화 시 같은 시험을 찾는 기준 */
    @Column(name = "exam_code", length = 20, unique = true)
    private String examCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExamCategory category;

    @Column(length = 100)
    private String organizer;

    /** 유효기간(개월). null이면 영구 */
    @Column(name = "validity_months")
    private Integer validityMonths;

    @Column(name = "official_url", length = 300)
    private String officialUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ExamSource source;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Exam(String examCode, String name, ExamCategory category, String organizer,
                 Integer validityMonths, String officialUrl, ExamSource source) {
        this.examCode = examCode;
        this.name = name;
        this.category = category;
        this.organizer = organizer;
        this.validityMonths = validityMonths;
        this.officialUrl = officialUrl;
        this.source = source;
    }

    public static Exam createManual(String examCode, String name, ExamCategory category, String organizer,
                                    Integer validityMonths, String officialUrl) {
        return new Exam(examCode, name, category, organizer, validityMonths, officialUrl, ExamSource.MANUAL);
    }

    /** examCode와 source는 동기화 기준이라 수정 대상에서 제외 */
    public void update(String name, ExamCategory category, String organizer,
                       Integer validityMonths, String officialUrl) {
        this.name = name;
        this.category = category;
        this.organizer = organizer;
        this.validityMonths = validityMonths;
        this.officialUrl = officialUrl;
    }
}
