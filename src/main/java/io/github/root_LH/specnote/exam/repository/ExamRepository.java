package io.github.root_LH.specnote.exam.repository;

import io.github.root_LH.specnote.exam.domain.Exam;
import io.github.root_LH.specnote.exam.domain.ExamCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    /**
     * 카테고리·키워드 검색. 값이 null인 조건은 무시됨.
     */
    @Query("""
            select e from Exam e
            where (:category is null or e.category = :category)
              and (:keyword is null or lower(e.name) like lower(concat('%', :keyword, '%')))
            """)
    Page<Exam> search(@Param("category") ExamCategory category,
                      @Param("keyword") String keyword,
                      Pageable pageable);

    boolean existsByExamCode(String examCode);
}
