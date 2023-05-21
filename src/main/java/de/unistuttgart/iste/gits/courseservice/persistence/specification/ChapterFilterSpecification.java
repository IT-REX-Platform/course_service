package de.unistuttgart.iste.gits.courseservice.persistence.specification;

import de.unistuttgart.iste.gits.common.util.SpecificationUtil;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.ChapterEntity;
import de.unistuttgart.iste.gits.generated.dto.ChapterFilterDto;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import java.util.UUID;

import static de.unistuttgart.iste.gits.common.util.SpecificationUtil.*;

public class ChapterFilterSpecification {

    public static Specification<ChapterEntity> courseIdEquals(UUID courseId) {
        return SpecificationUtil.equalTo("course", courseId);
    }

    public static Specification<ChapterEntity> chapterFilter(@Nullable ChapterFilterDto filterDto) {
        if (filterDto == null) {
            return null;
        }

        return Specification.allOf(
                        stringFilter("title", filterDto.getTitle()),
                        stringFilter("description", filterDto.getDescription()),
                        dateTimeFilter("startDate", filterDto.getStartDate()),
                        dateTimeFilter("endDate", filterDto.getEndDate()),
                        intFilter("number", filterDto.getNumber()),
                        and(filterDto.getAnd(), ChapterFilterSpecification::chapterFilter),
                        not(filterDto.getNot(), ChapterFilterSpecification::chapterFilter))
                .or(
                        or(filterDto.getOr(), ChapterFilterSpecification::chapterFilter));
    }

}
