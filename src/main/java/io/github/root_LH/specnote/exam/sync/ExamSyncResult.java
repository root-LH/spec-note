package io.github.root_LH.specnote.exam.sync;

/**
 * 동기화 결과
 *
 * [종목 등록]
 * - examsAdded: 종목 목록 API에서 새로 등록한 시험 수
 * - catalogFailed: 종목 목록 조회 실패 여부 (실패해도 기존 시험의 일정 동기화는 진행)
 *
 * [일정 동기화]
 * - examCount: 일정을 조회한 시험 수
 * - created / updated / unchanged: 새로 추가 / 날짜 변경 / 변경 없음
 * - invalid: 날짜 순서가 맞지 않아 건너뛴 일정
 * - failedExams: API 호출에 실패한 시험 수
 */
public record ExamSyncResult(
        int examsAdded,
        boolean catalogFailed,
        int examCount,
        int created,
        int updated,
        int unchanged,
        int invalid,
        int failedExams
) {
}
