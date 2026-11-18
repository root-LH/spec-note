package io.github.root_LH.specnote.subscription.controller;

import io.github.root_LH.specnote.subscription.dto.SubscriptionResponse;
import io.github.root_LH.specnote.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Subscription", description = "관심 시험 구독")
@RestController
@RequestMapping("/api/me/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "내 구독 목록")
    @GetMapping
    public List<SubscriptionResponse> getMySubscriptions(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId) {
        return subscriptionService.getMySubscriptions(memberId);
    }

    @Operation(summary = "시험 구독", description = "이미 구독 중이어도 204 (멱등)")
    @PostMapping("/{examId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void subscribe(@Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
                          @PathVariable Long examId) {
        subscriptionService.subscribe(memberId, examId);
    }

    @Operation(summary = "구독 해제", description = "구독하지 않은 시험이어도 204 (멱등)")
    @DeleteMapping("/{examId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
                            @PathVariable Long examId) {
        subscriptionService.unsubscribe(memberId, examId);
    }
}
