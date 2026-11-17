package io.github.root_LH.specnote.auth.controller;

import io.github.root_LH.specnote.auth.dto.LoginRequest;
import io.github.root_LH.specnote.auth.dto.RefreshRequest;
import io.github.root_LH.specnote.auth.dto.SignupRequest;
import io.github.root_LH.specnote.auth.dto.SignupResponse;
import io.github.root_LH.specnote.auth.dto.TokenResponse;
import io.github.root_LH.specnote.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "회원가입·로그인·토큰 재발급")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signup(@Valid @RequestBody SignupRequest request) {
        return new SignupResponse(authService.signup(request));
    }

    @Operation(summary = "로그인", description = "Access Token과 Refresh Token 발급")
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새 토큰 발급")
    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }
}
