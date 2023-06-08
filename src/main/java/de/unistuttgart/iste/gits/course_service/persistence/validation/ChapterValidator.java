package de.unistuttgart.iste.gits.course_service.persistence.validation;

import de.unistuttgart.iste.gits.generated.dto.CreateChapterInputDto;
import de.unistuttgart.iste.gits.generated.dto.UpdateChapterInputDto;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

/**
 * Validates chapter related input.
 */
@Component
public class ChapterValidator {

    public void validateCreateChapterInputDto(CreateChapterInputDto input) {
        if (input.getStartDate().isAfter(input.getEndDate())) {
            throw new ValidationException("Start date must be before end date");
        }
    }

    public void validateUpdateChapterInputDto(UpdateChapterInputDto input) {
        if (input.getStartDate().isAfter(input.getEndDate())) {
            throw new ValidationException("Start date must be before end date");
        }
    }
}
