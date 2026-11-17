package io.github.root_LH.specnote.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @CreatedDate, @LastModifiedDate 자동 입력을 켬.
 * 메인 클래스가 아니라 별도 설정 클래스에 두면 @WebMvcTest 같은 슬라이스 테스트가 깨지지 않음.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
