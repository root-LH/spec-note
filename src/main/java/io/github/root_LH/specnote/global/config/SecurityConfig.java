package io.github.root_LH.specnote.global.config;

import io.github.root_LH.specnote.global.jwt.JwtAccessDeniedHandler;
import io.github.root_LH.specnote.global.jwt.JwtAuthenticationEntryPoint;
import io.github.root_LH.specnote.global.jwt.JwtAuthenticationFilter;
import io.github.root_LH.specnote.global.jwt.JwtProperties;
import io.github.root_LH.specnote.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // JWT를 쓰는 REST API라 세션, CSRF, 기본 로그인 화면은 사용하지 않음
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        // 시험·일정 조회는 비회원도 가능 (2단계에서 구현)
                        .requestMatchers(HttpMethod.GET, "/api/exams/**", "/api/schedules/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)  // 401
                        .accessDeniedHandler(accessDeniedHandler))           // 403

                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 기본은 BCrypt, 저장 형식에 {bcrypt} 접두어가 붙어 나중에 알고리즘을 바꾸기 쉬움
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
