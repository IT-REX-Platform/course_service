package de.unistuttgart.iste.gits.course_service.service;

import de.unistuttgart.iste.gits.common.event.CrudOperation;
import de.unistuttgart.iste.gits.common.util.PaginationUtil;
import de.unistuttgart.iste.gits.common.util.SortUtil;
import de.unistuttgart.iste.gits.course_service.dapr.TopicPublisher;
import de.unistuttgart.iste.gits.course_service.persistence.entity.ChapterEntity;
import de.unistuttgart.iste.gits.course_service.persistence.mapper.ChapterMapper;
import de.unistuttgart.iste.gits.course_service.persistence.repository.ChapterRepository;
import de.unistuttgart.iste.gits.course_service.persistence.validation.ChapterValidator;
import de.unistuttgart.iste.gits.generated.dto.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
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
    private final TopicPublisher topicPublisher;

    /**
     * Gets all chapters with the given ids.
     * @param ids The ids of the chapters to get.
     * @return The chapters with the given ids in order of the given ids.
     * @throws EntityNotFoundException If at least one of the chapters could not be found.
     */
    public List<Chapter> getChaptersByIds(final List<UUID> ids) {
        return chapterRepository.getAllByIdPreservingOrder(ids).stream()
                .map(chapterMapper::entityToDto)
                .toList();
    }

    /**
     * Creates a chapter.
     *
     * @param chapterData The data of the chapter to create.
     * @return The created chapter.
     * @throws EntityNotFoundException If the course with the given id does not exist.
     */
    public Chapter createChapter(final CreateChapterInput chapterData) {
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
    public Chapter updateChapter(final UpdateChapterInput chapterData) {
        chapterValidator.validateUpdateChapterInput(chapterData);

        final UUID courseID = requireChapterExisting(chapterData.getId())
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
    public UUID deleteChapter(final UUID uuid) {
        requireChapterExisting(uuid);

        chapterRepository.deleteById(uuid);

        //notify other chapter-dependent services of chapter deletion
        topicPublisher.notifyChapterChanges(List.of(uuid), CrudOperation.DELETE);

        return uuid;
    }

    /**
     * Gets the course for a chapter.
     *
     * @param chapterId The id of the chapter to get the course for.
     * @return The course of the chapter.
     */
    public Course getCourseForChapterId(final UUID chapterId) {
        final ChapterEntity chapterEntity = requireChapterExisting(chapterId);

        return courseService.getCourseById(chapterEntity.getCourseId());
    }

    /**
     * Checks if a chapter exists.
     *
     * @param uuid The id of the chapter to check.
     * @throws EntityNotFoundException If the chapter does not exist.
     * @return The chapter entity with the given id.
     */
    private ChapterEntity requireChapterExisting(final UUID uuid) {
        return chapterRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Chapter with id " + uuid + " not found"));
    }

    public ChapterPayload getChapters(final UUID courseId,
                                      @Nullable final ChapterFilter filter,
                                      final List<String> sortBy,
                                      final List<SortDirection> sortDirection,
                                      @Nullable final Pagination pagination) {
        courseService.requireCourseExisting(courseId);

        final Sort sort = SortUtil.createSort(sortBy, sortDirection);
        final Pageable pageRequest = PaginationUtil.createPageable(pagination, sort);

        final Specification<ChapterEntity> specification =
                where(courseIdEquals(courseId)).and(chapterFilter(filter));

        if (pageRequest.isPaged()) {
            final Page<ChapterEntity> result = chapterRepository.findAll(specification, pageRequest);
            return createChapterPayloadPaged(result);
        }

        final List<ChapterEntity> result = chapterRepository.findAll(specification, sort);
        return createChapterPayloadUnpaged(result);
    }

    private ChapterPayload createChapterPayloadPaged(final Page<ChapterEntity> chapters) {
        return chapterMapper.createChapterPayload(chapters.stream(),
                PaginationUtil.createPaginationInfo(chapters));
    }

    private ChapterPayload createChapterPayloadUnpaged(final List<ChapterEntity> chapters) {
        return chapterMapper.createChapterPayload(chapters.stream(),
                PaginationUtil.unpagedPaginationInfo(chapters.size()));
    }
}
