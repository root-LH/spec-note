package io.github.root_LH.specnote.notification.repository;

import io.github.root_LH.specnote.notification.domain.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
}
