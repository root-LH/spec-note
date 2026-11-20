package io.github.root_LH.specnote.exam.sync;

import io.github.root_LH.specnote.exam.domain.Exam;
import io.github.root_LH.specnote.exam.external.QnetExamItem;
import io.github.root_LH.specnote.exam.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Q-Net 종목 목록 → exam 테이블에 없는 종목만 새로 등록.
 * 이미 있는 종목은 건드리지 않음 (관리자가 이름 등을 고쳤을 수 있으므로).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamCatalogImporter {

    private static final String NATIONAL_TECH = "T";

    private final ExamRepository examRepository;

    /**
     * @param grades 등록할 등급 (예: [기사], [기사, 산업기사]) → QnetExamItem.grade() 기준
     * @return 새로 등록한 시험 수
     */
    @Transactional
    public int importNew(List<QnetExamItem> catalog, Collection<String> grades) {
        List<QnetExamItem> targets = catalog.stream()
                .filter(item -> isTarget(item, grades))
                .toList();

        Set<String> existingCodes = examRepository.findAllByExamCodeIsNotNull().stream()
                .map(Exam::getExamCode)
                .collect(Collectors.toSet());

        int added = 0;
        for (QnetExamItem item : targets) {
            // Set.add가 false면 이미 있거나, 목록 안에 같은 코드가 중복으로 온 경우
            if (existingCodes.add(item.jmCd())) {
                examRepository.save(Exam.createFromQnet(item.jmCd(), item.name()));
                added++;
            }
        }

        // 0건일 때 원인을 바로 알 수 있도록 단계별 개수를 남김
        log.info("[종목 등록] 전체 종목={} 대상 등급={} 대상={} 새로 등록={}",
                catalog.size(), grades, targets.size(), added);
        return added;
    }

    private boolean isTarget(QnetExamItem item, Collection<String> grades) {
        boolean nationalTech = item.qualGbCd() == null || NATIONAL_TECH.equals(item.qualGbCd());
        return nationalTech && grades.contains(item.grade());
    }
}
