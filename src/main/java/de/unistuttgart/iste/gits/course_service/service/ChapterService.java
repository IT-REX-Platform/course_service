package de.unistuttgart.iste.gits.course_service.service;

import de.unistuttgart.iste.gits.common.util.PaginationUtil;
import de.unistuttgart.iste.gits.common.util.SortUtil;
import de.unistuttgart.iste.gits.course_service.persistence.dao.ChapterEntity;
import de.unistuttgart.iste.gits.course_service.persistence.mapper.ChapterMapper;
import de.unistuttgart.iste.gits.course_service.persistence.repository.ChapterRepository;
import de.unistuttgart.iste.gits.course_service.persistence.validation.ChapterValidator;
import de.unistuttgart.iste.gits.generated.dto.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static de.unistuttgart.iste.gits.course_service.persistence.specification.ChapterFilterSpecification.chapterFilter;
import static de.unistuttgart.iste.gits.course_service.persistence.specification.ChapterFilterSpecification.courseIdEquals;
import static org.springframework.data.jpa.domain.Specification.where;

/**
 * Service that handles chapter related operations.
 */
@Service
@RequiredArgsConstructor
public class ChapterService {

    private final ChapterMapper chapterMapper;
    private final ChapterRepository chapterRepository;
    private final CourseService courseService;
    private final ChapterValidator chapterValidator;

    /**
     * Creates a chapter.
     *
     * @param chapterData The data of the chapter to create.
     * @return The created chapter.
     * @throws EntityNotFoundException If the course with the given id does not exist.
     */
    public Chapter createChapter(CreateChapterInput chapterData) {
        chapterValidator.validateCreateChapterInput(chapterData);
        courseService.requireCourseExisting(chapterData.getCourseId());

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
    public Chapter updateChapter(UpdateChapterInput chapterData) {
        chapterValidator.validateUpdateChapterInput(chapterData);
        requireChapterExisting(chapterData.getId());

        UUID courseID = chapterRepository.findById(chapterData.getId())
                .orElseThrow()
                .getCourseId();

        ChapterEntity updatedChapterEntity = chapterMapper.dtoToEntity(chapterData);
        updatedChapterEntity.setCourseId(courseID);
        updatedChapterEntity = chapterRepository.save(updatedChapterEntity);

        return chapterMapper.entityToDto(updatedChapterEntity);
    }

    /**
     * Deletes a chapter.
     *
     * @param uuid The id of the chapter to delete.
     * @return The id of the deleted chapter.
     * @throws EntityNotFoundException If the chapter does not exist.
     */
    public UUID deleteChapter(UUID uuid) {
        requireChapterExisting(uuid);

        chapterRepository.deleteById(uuid);
        return uuid;
    }

    /**
     * Checks if a chapter exists.
     *
     * @param uuid The id of the chapter to check.
     * @throws EntityNotFoundException If the chapter does not exist.
     */
    private void requireChapterExisting(UUID uuid) {
        if (!chapterRepository.existsById(uuid)) {
            throw new EntityNotFoundException("Chapter with id " + uuid + " not found");
        }
    }

    public ChapterPayload getChapters(UUID courseId,
                                      @Nullable ChapterFilter filter,
                                      List<String> sortBy,
                                      List<SortDirection> sortDirection,
                                      @Nullable Pagination pagination) {
        courseService.requireCourseExisting(courseId);

        Sort sort = SortUtil.createSort(sortBy, sortDirection);
        Pageable pageRequest = PaginationUtil.createPageable(pagination, sort);

        Specification<ChapterEntity> specification =
                where(courseIdEquals(courseId)).and(chapterFilter(filter));

        if (pageRequest.isPaged()) {
            Page<ChapterEntity> result = chapterRepository.findAll(specification, pageRequest);
            return createChapterPayloadPaged(result);
        }

        List<ChapterEntity> result = chapterRepository.findAll(specification, sort);
        return createChapterPayloadUnpaged(result);
    }

    private ChapterPayload createChapterPayloadPaged(Page<ChapterEntity> chapters) {
        return chapterMapper.createChapterPayload(chapters.stream(),
                PaginationUtil.createPaginationInfo(chapters));
    }

    private ChapterPayload createChapterPayloadUnpaged(List<ChapterEntity> chapters) {
        return chapterMapper.createChapterPayload(chapters.stream(),
                PaginationUtil.unpagedPaginationInfo(chapters.size()));
    }
}
