package de.unistuttgart.iste.gits.course_service.service;

import de.unistuttgart.iste.gits.common.dapr.CourseAssociationDTO;
import de.unistuttgart.iste.gits.common.dapr.CrudOperation;
import de.unistuttgart.iste.gits.course_service.persistence.dao.ChapterEntity;
import de.unistuttgart.iste.gits.course_service.persistence.dao.CourseEntity;
import de.unistuttgart.iste.gits.course_service.persistence.dao.CourseResourceAssociationEntity;
import de.unistuttgart.iste.gits.course_service.persistence.dao.ResourcePk;
import de.unistuttgart.iste.gits.course_service.persistence.repository.ChapterRepository;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.course_service.persistence.repository.ResourceRepository;
import de.unistuttgart.iste.gits.generated.dto.CourseResourceAssociation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service that takes care of all operations regarding resources of a course.
 */
@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;

    /**
     * Method that returns a List of all courses, with the availability of that resource grouped by a resource.
     *
     * @param resourceIds List of resource IDs
     * @return List of resource DTOs that contain a resource ID, List of Course IDs with their current availability
     */
    public List<CourseResourceAssociation> getCoursesByResourceId(List<UUID> resourceIds) {
        ArrayList<CourseResourceAssociation> resultList = new ArrayList<>();

        //resources can be part of multiple courses for which each a resource Entities is present
        List<CourseResourceAssociationEntity> resourceEntities;
        List<CourseEntity> courseEntities;

        for (UUID resourceId : resourceIds) {
            //retrieve all resource entities for a resource ID
            resourceEntities = resourceRepository.findResourceEntitiesByResourceIdOrderByCourseIdAsc(resourceId);

            //skip if resource ID does not exist
            if (resourceEntities == null || resourceEntities.isEmpty())
                continue;

            // retrieve all courses. Uses a stream to collect course IDs from the resource Entities
            // and in the end another stream to turn result into a list
            courseEntities = courseRepository.findAllById(
                    resourceEntities.stream().map(
                            CourseResourceAssociationEntity::getCourseId
            ).toList());

            resultList.add(createResourceDTO(resourceId, courseEntities));
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
    private CourseResourceAssociation createResourceDTO(UUID resourceId, List<CourseEntity> courses) {
        //init
        OffsetDateTime currentTime = OffsetDateTime.now();
        CourseResourceAssociation resource = CourseResourceAssociation.builder().setId(resourceId)
                .setAvailableCourses(new ArrayList<>())
                .setUnAvailableCourses(new ArrayList<>())
                .build();

        //add courses to available or unavailable list
        for (CourseEntity course : courses) {
            if (isAvailable(course, currentTime))
                resource.getAvailableCourses().add(course.getId());
            else
                resource.getUnAvailableCourses().add(course.getId());
        }

        return resource;

    }

    /**
     * Helper function to check if a resource is available in a course
     * @param courseEntity a course Entity
     * @param currentTime time stamp
     * @return a resource's availability
     */
    private boolean isAvailable(CourseEntity courseEntity, OffsetDateTime currentTime){
        //1st check: course is published
        if (courseEntity.isPublished())
            //2nd check: the course has already started but has not ended yet
            return courseEntity.getStartDate().isBefore(currentTime) && courseEntity.getEndDate().isAfter(currentTime);

        return false;
    }

    /**
     * Creates & Deletes Course-Resource Associations depending on input data
     * @param dto Association description including CRUD Operation to be performed on Association
     */
    public void updateResourceAssociations(CourseAssociationDTO dto){

        List<CourseResourceAssociationEntity> currentAssociations;
        List<CourseResourceAssociationEntity> dtoAssociations = new ArrayList<>();

        // completeness check of input
        if (dto.getResourceId() == null || dto.getChapterIds() == null || dto.getOperation() == null){
            throw new NullPointerException("incomplete message received: all fields of a message must be non-null");
        }

        for (UUID chapterId: dto.getChapterIds()) {
            ChapterEntity chapterEntity;
            CourseEntity courseEntity;

            try {
                // retrieve the course Entity by the Chapter ID
                chapterEntity = chapterRepository.findById(chapterId).orElseThrow();

                courseEntity = courseRepository.findCourseEntityByChaptersContaining(chapterEntity).orElseThrow();
            }catch (Exception e){
                continue;
            }

            ResourcePk primary =  ResourcePk.builder().resourceId(dto.getResourceId()).courseId(courseEntity.getId()).build();

            switch (dto.getOperation()){
                case CREATE:
                    if (!resourceRepository.existsById(primary)){
                        resourceRepository.save(CourseResourceAssociationEntity.builder()
                                .resourceId(dto.getResourceId())
                                .courseId(courseEntity.getId())
                                .build());
                    }
                    break;
                case UPDATE:
                    CourseResourceAssociationEntity entity = CourseResourceAssociationEntity.builder()
                            .resourceId(dto.getResourceId())
                            .courseId(courseEntity.getId())
                            .build();
                    // skip adding if already entity exists in database
                    if (!resourceRepository.existsById(primary)){
                        resourceRepository.save(entity);
                    }
                    dtoAssociations.add(entity);
                    break;
                case DELETE:
                    if (resourceRepository.existsById(primary)) {
                        resourceRepository.deleteById(primary);
                    }
                    break;
                default:
                    // do nothing
            }

            // in UPDATES multiple associations can be added or removed. here we remove all outdated associations
            if (dto.getOperation().equals(CrudOperation.UPDATE)){
                currentAssociations = resourceRepository.findResourceEntitiesByResourceIdOrderByCourseIdAsc(dto.getResourceId());
                // remove resource associations that are not part of the updated associations
                currentAssociations.stream().filter(entity -> !dtoAssociations.contains(entity)).forEach(resourceRepository::delete);
            }

        }
    }
}
