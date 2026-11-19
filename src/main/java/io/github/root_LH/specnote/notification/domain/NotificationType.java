package io.github.root_LH.specnote.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * 알림 종류.
 * daysBefore: 기준 날짜 며칠 전에 보낼지 (오늘 + daysBefore = 기준 날짜인 대상에게 발송)
 */
@Getter
@RequiredArgsConstructor
public enum NotificationType {

    // 시험 일정 알림 (target_id = schedule_id)
    REG_START_D3("접수 시작 3일 전", 3, ScheduleDateField.REG_START),
    REG_END_D1("접수 마감 1일 전", 1, ScheduleDateField.REG_END),
    EXAM_D1("시험 1일 전", 1, ScheduleDateField.EXAM_DATE),
    RESULT_D0("합격 발표일", 0, ScheduleDateField.RESULT_DATE),

    // 스펙 만료 알림 (target_id = spec_id)
    EXPIRE_D30("유효기간 만료 30일 전", 30, null),
    EXPIRE_D7("유효기간 만료 7일 전", 7, null);

    private final String description;
    private final int daysBefore;
    private final ScheduleDateField dateField;

    public enum ScheduleDateField {
        REG_START, REG_END, EXAM_DATE, RESULT_DATE
    }

    public boolean isScheduleType() {
        return dateField != null;
    }

    public boolean isSpecType() {
        return dateField == null;
    }

    /** 오늘 보내야 하는 알림의 기준 날짜 */
    public LocalDate targetDate(LocalDate today) {
        return today.plusDays(daysBefore);
    }

    public static List<NotificationType> scheduleTypes() {
        return Arrays.stream(values()).filter(NotificationType::isScheduleType).toList();
    }

    public static List<NotificationType> specTypes() {
        return Arrays.stream(values()).filter(NotificationType::isSpecType).toList();
    }
}
