package io.github.root_LH.specnote.subscription.dto;

import io.github.root_LH.specnote.exam.domain.Exam;
import io.github.root_LH.specnote.exam.domain.ExamCategory;
import io.github.root_LH.specnote.subscription.domain.Subscription;

import java.time.LocalDateTime;

public record SubscriptionResponse(
        Long examId,
        String examName,
        ExamCategory category,
        String categoryName,
        LocalDateTime subscribedAt
) {

    public static SubscriptionResponse from(Subscription subscription) {
        Exam exam = subscription.getExam();
        return new SubscriptionResponse(
                exam.getId(),
                exam.getName(),
                exam.getCategory(),
                exam.getCategory().getDescription(),
                subscription.getCreatedAt());
    }
}
