package io.github.root_LH.specnote.notification.repository;

import io.github.root_LH.specnote.notification.domain.NotificationLog;
import io.github.root_LH.specnote.notification.domain.NotificationStatus;
import io.github.root_LH.specnote.notification.domain.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    boolean existsByMemberIdAndTypeAndTargetId(Long memberId, NotificationType type, Long targetId);

    /** 재시도 대상: 실패했고 재시도 횟수가 남은 것 (한 번에 최대 100건) */
    List<NotificationLog> findTop100ByStatusAndRetryCountLessThanOrderByIdAsc(NotificationStatus status, int maxRetry);

    /** 내 알림 내역 (최근 30건) */
    List<NotificationLog> findTop30ByMemberIdOrderByIdDesc(Long memberId);
}
