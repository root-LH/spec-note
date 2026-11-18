package io.github.root_LH.specnote.notification.service;

import io.github.root_LH.specnote.notification.domain.NotificationSetting;
import io.github.root_LH.specnote.notification.dto.NotificationLogResponse;
import io.github.root_LH.specnote.notification.dto.NotificationSettingResponse;
import io.github.root_LH.specnote.notification.dto.NotificationSettingUpdateRequest;
import io.github.root_LH.specnote.notification.repository.NotificationLogRepository;
import io.github.root_LH.specnote.notification.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationSettingService {

    private final NotificationSettingRepository settingRepository;
    private final NotificationLogRepository logRepository;

    /** 설정을 저장한 적이 없으면 기본값을 보여줌 (DB에 저장하지는 않음) */
    @Transactional(readOnly = true)
    public NotificationSettingResponse getSetting(Long memberId) {
        NotificationSetting setting = settingRepository.findById(memberId)
                .orElseGet(() -> NotificationSetting.defaultOf(memberId));
        return NotificationSettingResponse.from(setting);
    }

    public NotificationSettingResponse updateSetting(Long memberId, NotificationSettingUpdateRequest request) {
        NotificationSetting setting = settingRepository.findById(memberId)
                .orElseGet(() -> settingRepository.save(NotificationSetting.defaultOf(memberId)));
        setting.changeEmailEnabled(request.emailEnabled());
        return NotificationSettingResponse.from(setting);
    }

    @Transactional(readOnly = true)
    public List<NotificationLogResponse> getMyNotifications(Long memberId) {
        return logRepository.findTop30ByMemberIdOrderByIdDesc(memberId).stream()
                .map(NotificationLogResponse::from)
                .toList();
    }
}
