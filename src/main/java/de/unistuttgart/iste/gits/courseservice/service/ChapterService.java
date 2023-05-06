package de.unistuttgart.iste.gits.courseservice.service;

import de.unistuttgart.iste.gits.courseservice.dto.ChapterDto;
import de.unistuttgart.iste.gits.courseservice.dto.CreateChapterInputDto;
import de.unistuttgart.iste.gits.courseservice.dto.UpdateChapterInputDto;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.ChapterEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.CourseEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.mapper.ChapterMapper;
import de.unistuttgart.iste.gits.courseservice.persistence.repository.ChapterRepository;
import de.unistuttgart.iste.gits.courseservice.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.courseservice.persistence.validation.ChapterValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Service that handles chapter related operations.
 */
@Service
@RequiredArgsConstructor
public class ChapterService {

    private final ChapterMapper chapterMapper;
    private final ChapterRepository chapterRepository;
    private final CourseRepository courseRepository;
    private final ChapterValidator chapterValidator;

    /**
     * Creates a chapter.
     *
     * @param chapterData The data of the chapter to create.
     * @return The created chapter.
     * @throws EntityNotFoundException If the course with the given id does not exist.
     */
    public ChapterDto createChapter(CreateChapterInputDto chapterData) {
        chapterValidator.validateCreateChapterInputDto(chapterData);
        if (!courseRepository.existsById(chapterData.getCourseId())) {
            throw new EntityNotFoundException("Course with id " + chapterData.getCourseId() + " does not exist.");
        }

        ChapterEntity chapterEntity = chapterMapper.dtoToEntity(chapterData);
        chapterEntity = chapterRepository.save(chapterEntity);

        return chapterMapper.entityToDto(chapterEntity);
    }

    /**
     * Updates a chapter.
     *
     * @param chapterData The data of the chapter to update.
     * @return The updated chapter.
     */
    public ChapterDto updateChapter(UpdateChapterInputDto chapterData) {
        chapterValidator.validateUpdateChapterInputDto(chapterData);

        CourseEntity course = chapterRepository.findById(chapterData.getId())
                .orElseThrow(() -> new EntityNotFoundException("Chapter with id " + chapterData.getId() + " not found"))
                .getCourse();

        ChapterEntity updatedChapterEntity = chapterMapper.dtoToEntity(chapterData);
        updatedChapterEntity.setCourse(course);
        updatedChapterEntity = chapterRepository.save(updatedChapterEntity);

        return chapterMapper.entityToDto(updatedChapterEntity);
    }

    /**
     * Deletes a chapter.
     *
     * @param uuid The id of the chapter to delete.
     * @return The id of the deleted chapter or an empty optional if the chapter
     * does not exist.
     */
    public Optional<UUID> deleteChapter(UUID uuid) {
        if (!chapterRepository.existsById(uuid)) {
            return Optional.empty();
        }

        chapterRepository.deleteById(uuid);
        return Optional.of(uuid);
    }
}
