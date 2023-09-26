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

/**
 * Service that handles course related operations.
 */
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final MembershipService membershipService;
    private final CourseMapper courseMapper;
    private final CourseValidator courseValidator;
    private final TopicPublisher topicPublisher;

    /**
     * Creates a course.
     *
     * @param courseInput The data of the course to create.
     * @return The created course.
     */
    public Course createCourse(final CreateCourseInput courseInput, final UUID userId) {
        courseValidator.validateCreateCourseInput(courseInput);

        final CourseEntity courseEntity = courseRepository.save(courseMapper.dtoToEntity(courseInput));

        // create Membership for the creator of the course
        final CourseMembershipInput courseMembershipInput = new CourseMembershipInput(userId, courseEntity.getId(), UserRoleInCourse.ADMINISTRATOR);

        membershipService.createMembership(courseMembershipInput);

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
        //collect chapters that would be deleted with the course due to cascading deletion
        final CourseEntity entity = requireCourseExisting(uuid);
        final List<UUID> chapterIds = entity.getChapters().stream().map(ChapterEntity::getId).toList();

        // delete Memberships
        membershipService.deleteMembershipByCourseId(uuid);
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
        return courseRepository.getAllByIdPreservingOrder(ids)
                .stream()
                .map(courseMapper::entityToDto)
                .toList();
    }

    /**
     * Returns a course by its id.
     *
     * @param courseId The id of the course to return.
     * @return The course with the given id.
     */
    public Course getCourseById(final UUID courseId) {
        final CourseEntity entity = requireCourseExisting(courseId);
        return courseMapper.entityToDto(entity);
    }

    /**
     * Checks if a course with the given id exists. If not, an EntityNotFoundException is thrown.
     *
     * @param id The id of the course to check.
     * @return The course with the given id.
     * @throws EntityNotFoundException If a course with the given id does not exist.
     */
    public CourseEntity requireCourseExisting(final UUID id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course with id " + id + " not found"));
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

    /**
     * Returns a map of courses by their course memberships.
     *
     * @param courseMemberships The course memberships to get the courses for.
     * @return A map of courses by their course memberships.
     */
    public Map<CourseMembership, Course> getCoursesByCourseMemberships(final List<CourseMembership> courseMemberships) {
        final List<UUID> courseIds = courseMemberships.stream()
                .map(CourseMembership::getCourseId)
                .toList();

        final List<Course> courses = getCoursesByIds(courseIds);

        final Map<CourseMembership, Course> courseMap = new HashMap<>();

        for (int i = 0; i < courseIds.size(); i++) {
            courseMap.put(courseMemberships.get(i), courses.get(i));
        }

        return courseMap;
    }

    private CoursePayload createCoursePayloadPaged(final Page<CourseEntity> result) {
        return courseMapper.createPayload(result.stream(), PaginationUtil.createPaginationInfo(result));
    }

    private CoursePayload createCoursePayloadUnpaged(final List<CourseEntity> result) {
        return courseMapper.createPayload(result.stream(), PaginationUtil.unpagedPaginationInfo(result.size()));
    }
}
