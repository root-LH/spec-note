package io.github.root_LH.specnote.notification.service;

import io.github.root_LH.specnote.notification.domain.NotificationLog;
import io.github.root_LH.specnote.notification.domain.NotificationType;
import io.github.root_LH.specnote.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 알림 기록 저장·상태 변경.
 * 메서드마다 트랜잭션이 따로 열리도록 별도 클래스로 분리함.
 * → 한 건이 실패해도(중복 등) 배치 전체가 롤백되지 않음
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationLogService {

    private final NotificationLogRepository logRepository;

    /**
     * PENDING 상태로 기록. 이미 같은 알림이 있으면 DB UNIQUE 제약으로
     * DataIntegrityViolationException이 발생 → 호출하는 쪽에서 "이미 보냄"으로 처리
     */
    public Long createPending(Long memberId, NotificationType type, Long targetId) {
        return logRepository.save(NotificationLog.pending(memberId, type, targetId)).getId();
    }

    public void markSent(Long logId, LocalDateTime sentAt) {
        logRepository.findById(logId).ifPresent(log -> log.markSent(sentAt));
    }

    public void markFailed(Long logId) {
        logRepository.findById(logId).ifPresent(NotificationLog::markFailed);
    }
}
