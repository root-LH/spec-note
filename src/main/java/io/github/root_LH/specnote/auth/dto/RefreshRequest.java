package io.github.root_LH.specnote.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank
        String refreshToken
) {
}
