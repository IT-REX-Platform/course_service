package de.unistuttgart.iste.gits.courseservice.service;

import de.unistuttgart.iste.gits.courseservice.persistence.dao.CourseEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.CourseResourceAssociationEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.courseservice.persistence.repository.ResourceRepository;
import de.unistuttgart.iste.gits.generated.dto.CourseResourceAssociationDto;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ResourceServiceTest {

    private final ResourceRepository resourceRepository = Mockito.mock(ResourceRepository.class);
    private final CourseRepository courseRepository = Mockito.mock(CourseRepository.class);

    private final ResourceService resourceService = new ResourceService(resourceRepository, courseRepository);

    /**
     * This test checks if a correct Resource DTO is created and has the correct availability of a resource for a course depending on the published variable.
     */
    @Test
    void testGetCoursesByResourceId() {
        //init data
        UUID resourceId = UUID.randomUUID();

        CourseEntity courseEntity = dummyCourseEntityBuilder(OffsetDateTime.now(), false).build();
        CourseEntity anotherCourseEntity = dummyCourseEntityBuilder(OffsetDateTime.now(), true).build();

        CourseResourceAssociationEntity courseResourceAssociationEntity = CourseResourceAssociationEntity.builder()
                .courseId(courseEntity.getId())
                .resourceId(resourceId)
                .build();
        CourseResourceAssociationEntity anotherCourseResourceAssociationEntity = CourseResourceAssociationEntity.builder()
                .courseId(anotherCourseEntity.getId())
                .resourceId(resourceId)
                .build();

        List<CourseResourceAssociationEntity> courseResourceAssociationEntityList = List.of(courseResourceAssociationEntity, anotherCourseResourceAssociationEntity);
        List<CourseEntity> courseEntityList = List.of(courseEntity, anotherCourseEntity);

        // expected: two courses have the same resource
        CourseResourceAssociationDto expectedDto = CourseResourceAssociationDto.builder()
                .setId(resourceId)
                .setAvailableCourses(List.of(courseEntityList.get(1).getId()))
                .setUnAvailableCourses(List.of(courseEntityList.get(0).getId()))
                .build();

        //mock repositories
        when(resourceRepository.findResourceEntitiesByResourceIdOrderByCourseIdAsc(any(UUID.class)))
                .thenReturn(courseResourceAssociationEntityList);
        when(courseRepository.findAllById(any(List.class))).thenReturn(courseEntityList);

        //run method under test
        List<CourseResourceAssociationDto> actualResult = resourceService.getCoursesByResourceId(List.of(resourceId));

        //compare result
        assertEquals(expectedDto, actualResult.get(0));
    }

    /**
     * This test checks if faulty/expired dates correctly cause a resource to be tagged as unavailable.
     */
    @Test
    void testAvailabilityCheckWithExpiredDate(){
        //init data
        UUID resourceId = UUID.randomUUID();

        CourseEntity courseEntity = invalidDummyCourseEntityBuilder(OffsetDateTime.now(), true).build();
        CourseEntity anotherCourseEntity = dummyCourseEntityBuilder(OffsetDateTime.now(), true).build();

        CourseResourceAssociationEntity courseResourceAssociationEntity = CourseResourceAssociationEntity.builder()
                .courseId(courseEntity.getId())
                .resourceId(resourceId)
                .build();
        CourseResourceAssociationEntity anotherCourseResourceAssociationEntity = CourseResourceAssociationEntity.builder()
                .courseId(anotherCourseEntity.getId())
                .resourceId(resourceId)
                .build();
        List<CourseResourceAssociationEntity> courseResourceAssociationEntityList = List.of(courseResourceAssociationEntity, anotherCourseResourceAssociationEntity);
        List<CourseEntity> courseEntityList = List.of(courseEntity, anotherCourseEntity);

        // expected: two courses share a resource.
        CourseResourceAssociationDto expectedDto = CourseResourceAssociationDto.builder()
                .setId(resourceId)
                .setAvailableCourses(List.of(courseEntityList.get(1).getId()))
                .setUnAvailableCourses(List.of(courseEntityList.get(0).getId()))
                .build();

        //mock repositories
        when(resourceRepository.findResourceEntitiesByResourceIdOrderByCourseIdAsc(any(UUID.class)))
                .thenReturn(courseResourceAssociationEntityList);
        when(courseRepository.findAllById(any(List.class))).thenReturn(courseEntityList);

        //run method under test
        List<CourseResourceAssociationDto> actualResult = resourceService.getCoursesByResourceId(List.of(resourceId));

        //compare result
        assertEquals(expectedDto, actualResult.get(0));
    }

    // Builder methods used for creating entities
    private CourseEntity.CourseEntityBuilder dummyCourseEntityBuilder(OffsetDateTime now, boolean published) {
        return CourseEntity.builder()
                .id(UUID.randomUUID())
                .title("title")
                .description("description")
                .startDate(now.minusMonths(1))
                .endDate(now.plusMonths(2))
                .published(published);

    }

    private CourseEntity.CourseEntityBuilder invalidDummyCourseEntityBuilder(OffsetDateTime now, boolean published) {
        return CourseEntity.builder()
                .id(UUID.randomUUID())
                .title("title")
                .description("description")
                .startDate(now.plusMonths(1))
                .endDate(now.minusMonths(2))
                .published(published);

    }

}