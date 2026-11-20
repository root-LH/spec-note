package io.github.root_LH.specnote.exam.sync;

/**
 * 동기화 진행 중 결과를 모으는 카운터 (같은 패키지 안에서만 사용)
 */
class SyncCounter {

    int examsAdded;
    boolean catalogFailed;
    int examCount;
    int created;
    int updated;
    int unchanged;
    int invalid;
    int failedExams;

    ExamSyncResult toResult() {
        return new ExamSyncResult(examsAdded, catalogFailed, examCount, created, updated, unchanged, invalid, failedExams);
    }
}
