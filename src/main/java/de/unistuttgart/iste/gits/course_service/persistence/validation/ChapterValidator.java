package de.unistuttgart.iste.gits.course_service.persistence.validation;

import de.unistuttgart.iste.gits.generated.dto.CreateChapterInput;
import de.unistuttgart.iste.gits.generated.dto.UpdateChapterInput;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Validates chapter related input.
 */
@Component
public class ChapterValidator {

    /**
     * Validates the input for creating a chapter.
     * A chapter is valid if:
     * <ul>
     *     <li>the start date is before the end date</li>
     *     <li>the suggested start date is after the start date</li>
     *     <li>the suggested start date is before the end date</li>
     *     <li>the suggested start date is before the suggested end date</li>
     *     <li>the suggested end date is after the start date</li>
     *     <li>the suggested end date is before the end date</li>>
     * </ul>
     */
    public void validateCreateChapterInput(CreateChapterInput input) {
        validateDates(input.getStartDate(), input.getEndDate(), input.getSuggestedStartDate(), input.getSuggestedEndDate());
    }

    /**
     * @see #validateCreateChapterInput(CreateChapterInput)
     */
    public void validateUpdateChapterInput(UpdateChapterInput input) {
        validateDates(input.getStartDate(), input.getEndDate(), input.getSuggestedStartDate(), input.getSuggestedEndDate());
    }

    private void validateDates(OffsetDateTime startDate, OffsetDateTime endDate, OffsetDateTime suggestedStartDate, OffsetDateTime suggestedEndDate) {
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date must be before end date");
        }
        if (suggestedStartDate != null) {
            if (suggestedStartDate.isBefore(startDate)) {
                throw new ValidationException("Suggested start date must be after start date");
            }
            if (suggestedStartDate.isAfter(endDate)) {
                throw new ValidationException("Suggested start date must be before end date");
            }
            if (suggestedStartDate.isAfter(suggestedEndDate)) {
                throw new ValidationException("Suggested start date must be before suggested end date");
            }
        }
        if (suggestedEndDate != null) {
            if (suggestedEndDate.isBefore(startDate)) {
                throw new ValidationException("Suggested end date must be after start date");
            }
            if (suggestedEndDate.isAfter(endDate)) {
                throw new ValidationException("Suggested end date must be before end date");
            }
        }
    }

}
