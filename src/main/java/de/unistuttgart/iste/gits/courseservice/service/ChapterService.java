package de.unistuttgart.iste.gits.courseservice.service;

import de.unistuttgart.iste.gits.courseservice.dto.*;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.ChapterEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.CourseEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.mapper.ChapterMapper;
import de.unistuttgart.iste.gits.courseservice.persistence.repository.ChapterRepository;
import de.unistuttgart.iste.gits.courseservice.persistence.validation.ChapterValidator;
import de.unistuttgart.iste.gits.courseservice.util.PaginationUtil;
import de.unistuttgart.iste.gits.courseservice.util.SortUtil;
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

import static de.unistuttgart.iste.gits.courseservice.persistence.specification.ChapterFilterSpecification.chapterFilter;
import static de.unistuttgart.iste.gits.courseservice.persistence.specification.ChapterFilterSpecification.courseIdEquals;
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
    public ChapterDto createChapter(CreateChapterInputDto chapterData) {
        chapterValidator.validateCreateChapterInputDto(chapterData);
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
    public ChapterDto updateChapter(UpdateChapterInputDto chapterData) {
        chapterValidator.validateUpdateChapterInputDto(chapterData);
        requireChapterExisting(chapterData.getId());

        CourseEntity course = chapterRepository.findById(chapterData.getId())
                .orElseThrow()
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

    public ChapterPayloadDto getChapters(UUID courseId,
                                         @Nullable ChapterFilterDto filter,
                                         List<String> sortBy,
                                         List<SortDirectionDto> sortDirection,
                                         @Nullable PaginationDto pagination) {
        courseService.requireCourseExisting(courseId);

        Sort sort = SortUtil.createSort(sortBy, sortDirection);
        Pageable pageRequest = PaginationUtil.createPageable(pagination, sort);

        Specification<ChapterEntity> specification = where(courseIdEquals(courseId))
                .and(chapterFilter(filter));

        if (pageRequest.isPaged()) {
            Page<ChapterEntity> result = chapterRepository.findAll(specification, pageRequest);
            return createChapterPayloadDtoPaged(result);
        }

        List<ChapterEntity> result = chapterRepository.findAll(specification, sort);
        return createChapterPayloadDtoUnpaged(result);
    }

    private ChapterPayloadDto createChapterPayloadDtoPaged(Page<ChapterEntity> chapters) {
        return ChapterPayloadDto.builder()
                .setElements(chapters.stream().map(chapterMapper::entityToDto).toList())
                .setPagination(PaginationUtil.createPaginationInfoDto(chapters))
                .build();
    }

    private ChapterPayloadDto createChapterPayloadDtoUnpaged(List<ChapterEntity> chapters) {
        return ChapterPayloadDto.builder()
                .setElements(chapters.stream().map(chapterMapper::entityToDto).toList())
                .setPagination(PaginationUtil.unpagedPaginationInfoDto(chapters.size()))
                .build();
    }
}
