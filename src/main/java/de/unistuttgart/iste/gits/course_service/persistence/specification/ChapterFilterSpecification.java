package de.unistuttgart.iste.gits.course_service.persistence.specification;

import de.unistuttgart.iste.gits.common.util.SpecificationUtil;
import de.unistuttgart.iste.gits.course_service.persistence.dao.ChapterEntity;
import de.unistuttgart.iste.gits.generated.dto.ChapterFilter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import java.util.UUID;

import static de.unistuttgart.iste.gits.common.util.SpecificationUtil.*;

public class ChapterFilterSpecification {

    private ChapterFilterSpecification() {
        // Utility class
    }

    public static Specification<ChapterEntity> courseIdEquals(UUID courseId) {
        return SpecificationUtil.equalTo("courseId", courseId);
    }

    public static Specification<ChapterEntity> chapterFilter(@Nullable ChapterFilter filter) {
        if (filter == null) {
            return null;
        }

        return Specification.allOf(
                        stringFilter("title", filter.getTitle()),
                        stringFilter("description", filter.getDescription()),
                        dateTimeFilter("startDate", filter.getStartDate()),
                        dateTimeFilter("endDate", filter.getEndDate()),
                        dateTimeFilter("suggestedStartDate", filter.getSuggestedStartDate()),
                        dateTimeFilter("suggestedEndDate", filter.getSuggestedEndDate()),
                        intFilter("number", filter.getNumber()),
                        and(filter.getAnd(), ChapterFilterSpecification::chapterFilter),
                        not(filter.getNot(), ChapterFilterSpecification::chapterFilter))
                .or(
                        or(filter.getOr(), ChapterFilterSpecification::chapterFilter));
    }

}
