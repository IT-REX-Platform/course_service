package de.unistuttgart.iste.gits.course_service.persistence.validation;

import de.unistuttgart.iste.gits.generated.dto.CreateCourseInput;
import de.unistuttgart.iste.gits.generated.dto.UpdateCourseInput;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

/**
 * Validates course related input.
 */
@Component
public class CourseValidator {

    public void validateCreateCourseInput(CreateCourseInput input) {
        if (input.getStartDate().isAfter(input.getEndDate())) {
            throw new ValidationException("Start date must be before end date");
        }
    }

    public void validateUpdateCourseInput(UpdateCourseInput input) {
        if (input.getStartDate().isAfter(input.getEndDate())) {
            throw new ValidationException("Start date must be before end date");
        }
    }
}
