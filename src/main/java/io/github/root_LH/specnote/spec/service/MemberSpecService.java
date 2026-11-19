package io.github.root_LH.specnote.spec.service;

import io.github.root_LH.specnote.exam.domain.Exam;
import io.github.root_LH.specnote.exam.repository.ExamRepository;
import io.github.root_LH.specnote.global.exception.BusinessException;
import io.github.root_LH.specnote.global.exception.ErrorCode;
import io.github.root_LH.specnote.member.repository.MemberRepository;
import io.github.root_LH.specnote.spec.domain.MemberSpec;
import io.github.root_LH.specnote.spec.dto.SpecRequest;
import io.github.root_LH.specnote.spec.dto.SpecResponse;
import io.github.root_LH.specnote.spec.repository.MemberSpecRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberSpecService {

    private final MemberSpecRepository specRepository;
    private final ExamRepository examRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<SpecResponse> getMySpecs(Long memberId) {
        LocalDate today = LocalDate.now(clock);
        return specRepository.findAllWithExamByMemberId(memberId).stream()
                .map(spec -> SpecResponse.of(spec, today))
                .toList();
    }

    public SpecResponse create(Long memberId, SpecRequest request) {
        MemberSpec spec = MemberSpec.create(
                memberRepository.getReferenceById(memberId),
                findExamOrNull(request.examId()),
                request.customName(),
                request.score(),
                request.certNumber(),
                request.acquiredDate(),
                request.expireDate());
        return SpecResponse.of(specRepository.save(spec), LocalDate.now(clock));
    }

    public SpecResponse update(Long memberId, Long specId, SpecRequest request) {
        MemberSpec spec = findMySpec(memberId, specId);
        spec.update(
                findExamOrNull(request.examId()),
                request.customName(),
                request.score(),
                request.certNumber(),
                request.acquiredDate(),
                request.expireDate());
        return SpecResponse.of(spec, LocalDate.now(clock));
    }

    public void delete(Long memberId, Long specId) {
        specRepository.delete(findMySpec(memberId, specId));
    }

    /**
     * 다른 회원의 스펙이면 403이 아니라 404로 응답
     * → 그 ID의 스펙이 존재한다는 사실 자체를 알려주지 않기 위함
     */
    private MemberSpec findMySpec(Long memberId, Long specId) {
        return specRepository.findByIdAndMemberId(specId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SPEC_NOT_FOUND));
    }

    private Exam findExamOrNull(Long examId) {
        if (examId == null) {
            return null;
        }
        return examRepository.findById(examId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));
    }
}
