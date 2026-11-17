package io.github.root_LH.specnote.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * V1__init.sql 의 MEMBER 테이블과 매핑.
 * 테이블은 Flyway가 만들고, JPA는 ddl-auto: validate 로 구조가 맞는지만 검사함.
 */
@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // JPA용 기본 생성자, 외부에서 new 금지
@EntityListeners(AuditingEntityListener.class)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Member(String email, String password, String nickname, Role role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
    }

    /** 일반 회원 생성. 비밀번호는 반드시 암호화된 값을 넘길 것 */
    public static Member createUser(String email, String encodedPassword, String nickname) {
        return new Member(email, encodedPassword, nickname, Role.USER);
    }
}
