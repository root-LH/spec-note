package io.github.root_LH.specnote.subscription.service;

import io.github.root_LH.specnote.exam.domain.Exam;
import io.github.root_LH.specnote.exam.repository.ExamRepository;
import io.github.root_LH.specnote.global.exception.BusinessException;
import io.github.root_LH.specnote.global.exception.ErrorCode;
import io.github.root_LH.specnote.member.domain.Member;
import io.github.root_LH.specnote.member.repository.MemberRepository;
import io.github.root_LH.specnote.subscription.domain.Subscription;
import io.github.root_LH.specnote.subscription.dto.SubscriptionResponse;
import io.github.root_LH.specnote.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final ExamRepository examRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getMySubscriptions(Long memberId) {
        return subscriptionRepository.findAllWithExamByMemberId(memberId).stream()
                .map(SubscriptionResponse::from)
                .toList();
    }

    /**
     * 멱등(idempotent): 이미 구독 중이면 아무것도 하지 않음.
     * 버튼을 두 번 눌러도 에러 없이 같은 결과가 되도록 함.
     */
    public void subscribe(Long memberId, Long examId) {
        if (subscriptionRepository.existsByMemberIdAndExamId(memberId, examId)) {
            return;
        }
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));
        // getReferenceById: SELECT 없이 ID만 가진 프록시를 가져옴 (FK 연결에는 ID만 있으면 충분)
        Member member = memberRepository.getReferenceById(memberId);

        subscriptionRepository.save(Subscription.of(member, exam));
    }

    /** 멱등: 구독하지 않은 시험이어도 에러 없이 끝냄 */
    public void unsubscribe(Long memberId, Long examId) {
        subscriptionRepository.findByMemberIdAndExamId(memberId, examId)
                .ifPresent(subscriptionRepository::delete);
    }
}
