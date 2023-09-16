package de.unistuttgart.iste.gits.course_service.service;

import de.unistuttgart.iste.gits.common.event.CourseAssociationEvent;
import de.unistuttgart.iste.gits.common.event.CrudOperation;
import de.unistuttgart.iste.gits.common.exception.IncompleteEventMessageException;
import de.unistuttgart.iste.gits.course_service.persistence.entity.*;
import de.unistuttgart.iste.gits.course_service.persistence.repository.*;
import de.unistuttgart.iste.gits.generated.dto.CourseResourceAssociation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Service that takes care of all operations regarding associated resources of a course.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseResourceAssociationService {

    private final CourseResourceAssociationRepository resourceRepository;
    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;

    /**
     * Method that returns a List of all courses, with the availability of that resource grouped by a resource.
     *
     * @param resourceIds List of resource IDs
     * @return List of resource DTOs that contain a resource ID, List of Course IDs with their current availability
     */
    public List<CourseResourceAssociation> getCourseResourceAssociations(List<UUID> resourceIds) {
        ArrayList<CourseResourceAssociation> resultList = new ArrayList<>();

        // resources can be part of multiple courses for which each a resource Entities is present
        List<CourseResourceAssociationEntity> resourceEntities;
        List<CourseEntity> courseEntities;

        for (UUID resourceId : resourceIds) {
            // retrieve all resource entities for a resource ID
            resourceEntities = resourceRepository.findCourseResourceAssociationEntitiesByResourceIdOrderByCourseIdAsc(resourceId);

            // skip if resource ID does not exist
            if (resourceEntities == null || resourceEntities.isEmpty()) {
                continue;
            }

            // retrieve all courses. Uses a stream to collect course IDs from the resource Entities
            // and in the end another stream to turn result into a list
            courseEntities = courseRepository.findAllById(
                    resourceEntities.stream().map(CourseResourceAssociationEntity::getCourseId).toList());

            resultList.add(createCourseResourceAssociationDto(resourceId, courseEntities));
        }
        return resultList;
    }

    /**
     * Creates a compact Resource DTO from given information, including availability of resource within a course.
     *
     * @param resourceId resource ID of resource
     * @param courses    Courses that contain the resource
     * @return Resource DTO containing resource ID, List of Courses including the availability of the resource within the course
     */
    private CourseResourceAssociation createCourseResourceAssociationDto(UUID resourceId, List<CourseEntity> courses) {
        //init
        OffsetDateTime currentTime = OffsetDateTime.now();
        CourseResourceAssociation resource = CourseResourceAssociation.builder().setId(resourceId)
                .setAvailableCourses(new ArrayList<>())
                .setUnAvailableCourses(new ArrayList<>())
                .build();

        //add courses to available or unavailable list
        for (CourseEntity course : courses) {
            if (isAvailable(course, currentTime)) {
                resource.getAvailableCourses().add(course.getId());
            } else {
                resource.getUnAvailableCourses().add(course.getId());
            }
        }

        return resource;

    }

    /**
     * Helper function to check if a resource is available in a course
     *
     * @param courseEntity a course Entity
     * @param currentTime  time stamp
     * @return a resource's availability
     */
    private boolean isAvailable(CourseEntity courseEntity, OffsetDateTime currentTime) {
        // 1st check: course is published
        if (courseEntity.isPublished()) {
            // 2nd check: the course has already started but has not ended yet
            return courseEntity.getStartDate().isBefore(currentTime) && courseEntity.getEndDate().isAfter(currentTime);
        }

        return false;
    }

    /**
     * Creates & Deletes Course-Resource Associations depending on input data
     *
     * @param dto Association description including CRUD Operation to be performed on Association
     */
    public void updateResourceAssociations(CourseAssociationEvent dto) throws IncompleteEventMessageException {

        List<CourseResourceAssociationEntity> currentAssociations;
        List<CourseResourceAssociationEntity> dtoAssociations = new ArrayList<>();

        // completeness check of input
        if (dto.getResourceId() == null || dto.getChapterIds() == null || dto.getOperation() == null) {
            throw new IncompleteEventMessageException(IncompleteEventMessageException.ERROR_INCOMPLETE_MESSAGE);
        }

        // perform operation for each Chapter ID
        for (UUID chapterId : dto.getChapterIds()) {

            CourseResourceAssociationEntity resourceAssociationEntity;

            ResourcePk primary = ResourcePk.builder()
                    .resourceId(dto.getResourceId())
                    .chapterId(chapterId)
                    .build();
            try {
                resourceAssociationEntity = performCrudOperation(primary, dto.getOperation());

                if (resourceAssociationEntity != null) {
                    dtoAssociations.add(resourceAssociationEntity);
                }
            } catch (NoSuchElementException e) {
                log.error(e.getMessage());
            }

        }
        // in UPDATES multiple associations can be added or removed. here we remove all outdated associations
        if (dto.getOperation().equals(CrudOperation.UPDATE)) {
            currentAssociations = resourceRepository.findCourseResourceAssociationEntitiesByResourceIdOrderByCourseIdAsc(dto.getResourceId());
            // remove resource associations that are not part of the updated associations
            currentAssociations.stream()
                    .filter(entity -> !dtoAssociations.contains(entity))
                    .forEach(resourceRepository::delete);
        }
    }

    /**
     * performs CREATE, UPDATE, and DELETE operations on CourseResourceAssociations.
     *
     * @param primary   Primary key for CourseResourceAssociations
     * @param operation type of Operation performed
     * @return a CourseResourceAssociations in case of an UPDATE operation. default is null
     * @throws NoSuchElementException if no entity exists for either course or chapter ID
     */
    private CourseResourceAssociationEntity performCrudOperation(ResourcePk primary, CrudOperation operation) throws NoSuchElementException {

        switch (operation) {
            case CREATE -> {
                if (!resourceRepository.existsById(primary)) {
                    CourseResourceAssociationEntity entity = CourseResourceAssociationEntity.builder()
                            .resourceId(primary.getResourceId())
                            .chapterId(primary.getChapterId())
                            .courseId(getCourseFromChapterId(primary.getChapterId()))
                            .build();

                    resourceRepository.save(entity);

                }
                return null;
            }
            case UPDATE -> {
                CourseResourceAssociationEntity entity = CourseResourceAssociationEntity.builder()
                        .resourceId(primary.getResourceId())
                        .chapterId(primary.getChapterId())
                        .courseId(getCourseFromChapterId(primary.getChapterId()))
                        .build();
                resourceRepository.save(entity);
                return entity;
            }
            case DELETE -> {
                if (resourceRepository.existsById(primary)) {
                    resourceRepository.deleteById(primary);
                }
                return null;
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * retrieves the course ID for a given Chapter ID
     *
     * @param chapterId Chapter of a course
     * @return course ID
     * @throws NoSuchElementException if no entity exists for chapter ID
     */
    private UUID getCourseFromChapterId(UUID chapterId) throws NoSuchElementException {
        ChapterEntity chapter = chapterRepository.findById(chapterId).orElseThrow();
        return chapter.getCourseId();
    }
}
