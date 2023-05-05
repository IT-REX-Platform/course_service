package de.unistuttgart.iste.gits.courseservice.persistence.validation;

import de.unistuttgart.iste.gits.courseservice.dto.CreateChapterInputDto;
import de.unistuttgart.iste.gits.courseservice.dto.UpdateChapterInputDto;
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
