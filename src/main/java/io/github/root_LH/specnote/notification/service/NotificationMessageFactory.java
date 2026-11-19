package io.github.root_LH.specnote.notification.service;

import io.github.root_LH.specnote.exam.domain.Exam;
import io.github.root_LH.specnote.exam.domain.ExamSchedule;
import io.github.root_LH.specnote.exam.domain.ExamStage;
import io.github.root_LH.specnote.notification.domain.NotificationType;
import io.github.root_LH.specnote.notification.sender.NotificationMessage;
import io.github.root_LH.specnote.spec.domain.MemberSpec;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * 알림 제목·본문 생성
 */
@Component
public class NotificationMessageFactory {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd(E)", Locale.KOREAN);

    public NotificationMessage forSchedule(NotificationType type, ExamSchedule schedule) {
        Exam exam = schedule.getExam();
        String examTitle = examTitle(exam, schedule);

        String title = "[SpecNote] %s - %s".formatted(examTitle, type.getDescription());
        String body = """
                %s 일정 알림입니다.

                · 접수 기간: %s ~ %s
                · 시험일: %s
                · 합격 발표: %s

                자세한 내용: %s
                """.formatted(
                examTitle,
                format(schedule.getRegStart()),
                format(schedule.getRegEnd()),
                format(schedule.getExamDate()),
                format(schedule.getResultDate()),
                orDash(exam.getOfficialUrl()));

        return new NotificationMessage(title, body);
    }

    public NotificationMessage forSpec(NotificationType type, MemberSpec spec) {
        String name = spec.displayName();
        String title = "[SpecNote] %s - %s".formatted(name, type.getDescription());
        String body = """
                보유 중인 스펙의 유효기간이 곧 만료됩니다.

                · 스펙: %s
                · 점수/등급: %s
                · 취득일: %s
                · 만료일: %s

                재응시가 필요하다면 시험 일정을 확인해보세요.
                %s
                """.formatted(
                name,
                orDash(spec.getScore()),
                format(spec.getAcquiredDate()),
                format(spec.getExpireDate()),
                spec.getExam() != null ? orDash(spec.getExam().getOfficialUrl()) : "");

        return new NotificationMessage(title, body);
    }

    /** 예: "정보처리기사 2027년 1회 필기", 단일 시험은 단계 생략 "TOEIC 1회차" */
    private String examTitle(Exam exam, ExamSchedule schedule) {
        String base = exam.getName() + " " + schedule.getRoundName();
        return schedule.getStage() == ExamStage.SINGLE
                ? base
                : base + " " + schedule.getStage().getDescription();
    }

    private String format(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "미정";
    }

    private String orDash(String value) {
        return value != null ? value : "-";
    }
}
