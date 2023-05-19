package de.unistuttgart.iste.gits.courseservice.persistence.specification;

import de.unistuttgart.iste.gits.courseservice.dto.CourseFilterDto;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.CourseEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import static de.unistuttgart.iste.gits.courseservice.persistence.specification.SpecificationUtil.*;


public class CourseFilterSpecification {

    public static Specification<CourseEntity> courseFilter(@NonNull CourseFilterDto filterDto) {
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
