package de.unistuttgart.iste.gits.course_service.persistence.validation;

import de.unistuttgart.iste.gits.generated.dto.CreateCourseInputDto;
import de.unistuttgart.iste.gits.generated.dto.UpdateCourseInputDto;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

/**
 * Validates course related input.
 */
@Component
public class CourseValidator {

    public void validateCreateCourseInputDto(CreateCourseInputDto input) {
        if (input.getStartDate().isAfter(input.getEndDate())) {
            throw new ValidationException("Start date must be before end date");
        }
    }

    public void validateUpdateCourseInputDto(UpdateCourseInputDto input) {
        if (input.getStartDate().isAfter(input.getEndDate())) {
            throw new ValidationException("Start date must be before end date");
        }
    }
}
