package de.unistuttgart.iste.gits.course_service.persistence.validation;

import de.unistuttgart.iste.gits.generated.dto.CreateChapterInput;
import de.unistuttgart.iste.gits.generated.dto.UpdateChapterInput;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

/**
 * Validates chapter related input.
 */
@Component
public class ChapterValidator {

    public void validateCreateChapterInput(CreateChapterInput input) {
        if (input.getStartDate().isAfter(input.getEndDate())) {
            throw new ValidationException("Start date must be before end date");
        }
    }

    public void validateUpdateChapterInput(UpdateChapterInput input) {
        if (input.getStartDate().isAfter(input.getEndDate())) {
            throw new ValidationException("Start date must be before end date");
        }
    }
}
