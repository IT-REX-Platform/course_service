package de.unistuttgart.iste.gits.course_service.service;

import de.unistuttgart.iste.gits.common.event.CrudOperation;
import de.unistuttgart.iste.gits.common.util.PaginationUtil;
import de.unistuttgart.iste.gits.common.util.SortUtil;
import de.unistuttgart.iste.gits.course_service.dapr.TopicPublisher;
import de.unistuttgart.iste.gits.course_service.persistence.entity.ChapterEntity;
import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.gits.course_service.persistence.mapper.CourseMapper;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.course_service.persistence.specification.CourseFilterSpecification;
import de.unistuttgart.iste.gits.course_service.persistence.validation.CourseValidator;
import de.unistuttgart.iste.gits.generated.dto.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that handles course related operations.
 */
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final CourseValidator courseValidator;
    private final TopicPublisher topicPublisher;

    /**
     * Creates a course.
     *
     * @param courseInput The data of the course to create.
     * @return The created course.
     */
    public Course createCourse(final CreateCourseInput courseInput) {
        courseValidator.validateCreateCourseInput(courseInput);

        final CourseEntity courseEntity = courseRepository.save(courseMapper.dtoToEntity(courseInput));

        return courseMapper.entityToDto(courseEntity);
    }

    /**
     * Updates a course.
     *
     * @param input The data of the course to update.
     * @return The updated course.
     */
    public Course updateCourse(final UpdateCourseInput input) {
        courseValidator.validateUpdateCourseInput(input);
        requireCourseExisting(input.getId());

        final CourseEntity updatedCourseEntity = courseRepository.save(courseMapper.dtoToEntity(input));

        return courseMapper.entityToDto(updatedCourseEntity);
    }

    /**
     * Deletes a course.
     *
     * @param uuid The id of the course to delete.
     * @return The id of the deleted course.
     * @throws EntityNotFoundException If a course with the given id does not exist.
     */
    public UUID deleteCourse(final UUID uuid) {
        requireCourseExisting(uuid);

        //collect chapters that would be deleted with the course due to cascading deletion
        final CourseEntity entity = courseRepository.getReferenceById(uuid);
        final List<UUID> chapterIds = entity.getChapters().stream().map(ChapterEntity::getId).toList();

        //delete course and any chapters
        courseRepository.delete(entity);

        //publish changes
        topicPublisher.notifyCourseChanges(uuid, CrudOperation.DELETE);
        topicPublisher.notifyChapterChanges(chapterIds, CrudOperation.DELETE);

        return uuid;
    }

    /**
     * Returns a list of courses by their ids.
     *
     * @param ids The ids of the courses to return.
     * @return A list of courses with the given ids, preserving the order of the ids.
     * @throws EntityNotFoundException If a course with at least one of the given ids does not exist.
     */
    public List<Course> getCoursesByIds(final List<UUID> ids) {
        final var result = new ArrayList<Course>(ids.size());
        final var missingIds = new ArrayList<UUID>();

        for (final var id : ids) {
            courseRepository.findById(id)
                    .ifPresentOrElse(
                            courseEntity -> result.add(courseMapper.entityToDto(courseEntity)),
                            () -> missingIds.add(id));
        }

        if (!missingIds.isEmpty()) {
            throw new EntityNotFoundException("Course(s) with id(s) "
                                              + missingIds.stream().map(UUID::toString).collect(Collectors.joining(", "))
                                              + " not found");
        }

        return result;
    }

    /**
     * Checks if a course with the given id exists. If not, an EntityNotFoundException is thrown.
     *
     * @param id The id of the course to check.
     * @throws EntityNotFoundException If a course with the given id does not exist.
     */
    public void requireCourseExisting(final UUID id) {
        if (!courseRepository.existsById(id)) {
            throw new EntityNotFoundException("Course with id " + id + " not found");
        }
    }

    /**
     * Returns a list of all courses.
     *
     * @param filter        optional filter for the courses
     * @param sortBy        list of sort fields
     * @param sortDirection list of sort directions
     * @param pagination    optional pagination
     * @return a list of all courses
     */
    public CoursePayload getCourses(final CourseFilter filter,
                                    final List<String> sortBy,
                                    final List<SortDirection> sortDirection,
                                    final Pagination pagination) {

        final Sort sort = SortUtil.createSort(sortBy, sortDirection);
        final Pageable pageRequest = PaginationUtil.createPageable(pagination, sort);

        final Specification<CourseEntity> specification = CourseFilterSpecification.courseFilter(filter);

        if (pageRequest.isPaged()) {
            final Page<CourseEntity> result = courseRepository.findAll(specification, pageRequest);
            return createCoursePayloadPaged(result);
        }

        final List<CourseEntity> result = courseRepository.findAll(specification, sort);
        return createCoursePayloadUnpaged(result);
    }

    private CoursePayload createCoursePayloadPaged(final Page<CourseEntity> result) {
        return courseMapper.createPayload(result.stream(), PaginationUtil.createPaginationInfo(result));
    }

    private CoursePayload createCoursePayloadUnpaged(final List<CourseEntity> result) {
        return courseMapper.createPayload(result.stream(), PaginationUtil.unpagedPaginationInfo(result.size()));
    }

    public Course getCourseById(final UUID courseId) {
        final CourseEntity entity = courseRepository.getReferenceById(courseId);
        return courseMapper.entityToDto(entity);
    }
}
