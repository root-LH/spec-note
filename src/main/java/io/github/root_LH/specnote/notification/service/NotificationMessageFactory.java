package io.github.root_LH.specnote.notification.service;

import io.github.root_LH.specnote.exam.domain.Exam;
import io.github.root_LH.specnote.exam.domain.ExamSchedule;
import io.github.root_LH.specnote.exam.domain.ExamStage;
import io.github.root_LH.specnote.notification.domain.NotificationType;
import io.github.root_LH.specnote.notification.sender.NotificationMessage;
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
                exam.getOfficialUrl() != null ? exam.getOfficialUrl() : "-");

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
}
