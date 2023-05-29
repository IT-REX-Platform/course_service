package de.unistuttgart.iste.gits.courseservice.persistence.specification;

import de.unistuttgart.iste.gits.courseservice.persistence.dao.CourseEntity;
import de.unistuttgart.iste.gits.generated.dto.CourseFilterDto;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import static de.unistuttgart.iste.gits.common.util.SpecificationUtil.*;


public class CourseFilterSpecification {

    private CourseFilterSpecification() {
        // Utility class
    }

    public static Specification<CourseEntity> courseFilter(@Nullable CourseFilterDto filterDto) {
        if (filterDto == null) {
            return null;
        }
        return Specification.allOf(
                        stringFilter("title", filterDto.getTitle()),
                        stringFilter("description", filterDto.getDescription()),
                        dateTimeFilter("startDate", filterDto.getStartDate()),
                        dateTimeFilter("endDate", filterDto.getEndDate()),
                        booleanFilter("published", filterDto.getPublished()),
                        and(filterDto.getAnd(), CourseFilterSpecification::courseFilter),
                        not(filterDto.getNot(), CourseFilterSpecification::courseFilter))
                .or(
                        or(filterDto.getOr(), CourseFilterSpecification::courseFilter));
    }

}
