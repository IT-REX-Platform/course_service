package de.unistuttgart.iste.gits.course_service.service;

import de.unistuttgart.iste.gits.common.event.CourseAssociationEvent;
import de.unistuttgart.iste.gits.common.event.CrudOperation;
import de.unistuttgart.iste.gits.common.exception.IncompleteEventMessageException;
import de.unistuttgart.iste.gits.course_service.persistence.entity.*;
import de.unistuttgart.iste.gits.course_service.persistence.repository.*;
import de.unistuttgart.iste.gits.generated.dto.CourseResourceAssociation;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ResourceServiceTest {

    private final CourseResourceAssociationRepository resourceRepository = Mockito.mock(CourseResourceAssociationRepository.class);
    private final CourseRepository courseRepository = Mockito.mock(CourseRepository.class);

    private final ChapterRepository chapterRepository = Mockito.mock(ChapterRepository.class);

    private final CourseResourceAssociationService resourceService = new CourseResourceAssociationService(resourceRepository, courseRepository, chapterRepository);

    /**
     * This test checks if a correct Resource DTO is created and has the correct availability of a resource for a course depending on the published variable.
     */
    @Test
    void testGetCoursesByResourceId() {
        //init data
        final UUID resourceId = UUID.randomUUID();

        final CourseEntity courseEntity = dummyCourseEntityBuilder(OffsetDateTime.now(), false).build();
        final CourseEntity anotherCourseEntity = dummyCourseEntityBuilder(OffsetDateTime.now(), true).build();

        final CourseResourceAssociationEntity courseResourceAssociationEntity = CourseResourceAssociationEntity.builder()
                .courseId(courseEntity.getId())
                .resourceId(resourceId)
                .build();
        final CourseResourceAssociationEntity anotherCourseResourceAssociationEntity = CourseResourceAssociationEntity.builder()
                .courseId(anotherCourseEntity.getId())
                .resourceId(resourceId)
                .build();

        final List<CourseResourceAssociationEntity> courseResourceAssociationEntityList = List.of(courseResourceAssociationEntity, anotherCourseResourceAssociationEntity);
        final List<CourseEntity> courseEntityList = List.of(courseEntity, anotherCourseEntity);

        // expected: two courses have the same resource
        final CourseResourceAssociation expected = CourseResourceAssociation.builder()
                .setId(resourceId)
                .setAvailableCourses(List.of(courseEntityList.get(1).getId()))
                .setUnAvailableCourses(List.of(courseEntityList.get(0).getId()))
                .build();

        //mock repositories
        when(resourceRepository.findCourseResourceAssociationEntitiesByResourceIdOrderByCourseIdAsc(any(UUID.class)))
                .thenReturn(courseResourceAssociationEntityList);
        when(courseRepository.findAllById(any())).thenReturn(courseEntityList);

        //run method under test
        final List<CourseResourceAssociation> actualResult = resourceService.getCourseResourceAssociations(List.of(resourceId));

        //compare result
        assertEquals(expected, actualResult.get(0));
    }

    /**
     * This test checks if faulty/expired dates correctly cause a resource to be tagged as unavailable.
     */
    @Test
    void testAvailabilityCheckWithExpiredDate(){
        //init data
        final UUID resourceId = UUID.randomUUID();

        final CourseEntity courseEntity = invalidDummyCourseEntityBuilder(OffsetDateTime.now(), true).build();
        final CourseEntity anotherCourseEntity = dummyCourseEntityBuilder(OffsetDateTime.now(), true).build();

        final CourseResourceAssociationEntity courseResourceAssociationEntity = CourseResourceAssociationEntity.builder()
                .courseId(courseEntity.getId())
                .resourceId(resourceId)
                .build();
        final CourseResourceAssociationEntity anotherCourseResourceAssociationEntity = CourseResourceAssociationEntity.builder()
                .courseId(anotherCourseEntity.getId())
                .resourceId(resourceId)
                .build();
        final List<CourseResourceAssociationEntity> courseResourceAssociationEntityList = List.of(courseResourceAssociationEntity, anotherCourseResourceAssociationEntity);
        final List<CourseEntity> courseEntityList = List.of(courseEntity, anotherCourseEntity);

        // expected: two courses share a resource.
        final CourseResourceAssociation expected = CourseResourceAssociation.builder()
                .setId(resourceId)
                .setAvailableCourses(List.of(courseEntityList.get(1).getId()))
                .setUnAvailableCourses(List.of(courseEntityList.get(0).getId()))
                .build();

        //mock repositories
        when(resourceRepository.findCourseResourceAssociationEntitiesByResourceIdOrderByCourseIdAsc(any(UUID.class)))
                .thenReturn(courseResourceAssociationEntityList);
        when(courseRepository.findAllById(any())).thenReturn(courseEntityList);

        //run method under test
        final List<CourseResourceAssociation> actualResult = resourceService.getCourseResourceAssociations(List.of(resourceId));

        //compare result
        assertEquals(expected, actualResult.get(0));
    }

    @Test
    void testResourceCreation(){
        //init
        final CourseEntity courseEntity = dummyCourseEntityBuilder(OffsetDateTime.now(), true).build();
        final ChapterEntity chapterEntity = dummyChapterEntityBuilder(courseEntity.getId()).build();

        courseEntity.setChapters(List.of(chapterEntity));

        final CourseAssociationEvent dto = CourseAssociationEvent.builder().chapterIds(List.of(chapterEntity.getId())).resourceId(UUID.randomUUID()).operation(CrudOperation.CREATE).build();


        //mock repositories
        when(chapterRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(chapterEntity));
        when(courseRepository.findCourseEntityByChaptersContaining(any(ChapterEntity.class)))
                .thenReturn(Optional.of(courseEntity));

        //execute
        assertDoesNotThrow(() -> resourceService.updateResourceAssociations(dto));
    }

    @Test
    void testResourceCreationFromInvalidInput(){
        //init
        final UUID chapterId = UUID.randomUUID();
        final ChapterEntity chapterEntity = dummyChapterEntityBuilder(chapterId).build();
        final CourseAssociationEvent missingDbCourseEntity = CourseAssociationEvent.builder().chapterIds(List.of(chapterId)).resourceId(UUID.randomUUID()).operation(CrudOperation.CREATE).build();
        final CourseAssociationEvent missingDbChapterEntity = CourseAssociationEvent.builder().chapterIds(List.of(UUID.randomUUID())).resourceId(UUID.randomUUID()).operation(CrudOperation.DELETE).build();

        final CourseAssociationEvent missingChapter = CourseAssociationEvent.builder().resourceId(UUID.randomUUID()).operation(CrudOperation.CREATE).build();
        final CourseAssociationEvent missingResource = CourseAssociationEvent.builder().chapterIds(List.of(UUID.randomUUID())).operation(CrudOperation.CREATE).build();
        final CourseAssociationEvent missingOperator = CourseAssociationEvent.builder().chapterIds(List.of(UUID.randomUUID())).resourceId(UUID.randomUUID()).build();

        // mock repository
        when(chapterRepository.findById(chapterId))
                .thenReturn(Optional.of(chapterEntity));

        // act and assert
        // case: chapter or course does not exist
        assertDoesNotThrow(() -> resourceService.updateResourceAssociations(missingDbChapterEntity));
        assertDoesNotThrow(() -> resourceService.updateResourceAssociations(missingDbCourseEntity));

        // case: incomplete DTO
        assertThrows(IncompleteEventMessageException.class, () -> resourceService.updateResourceAssociations(missingChapter));
        assertThrows(IncompleteEventMessageException.class, () -> resourceService.updateResourceAssociations(missingResource));
        assertThrows(IncompleteEventMessageException.class, () -> resourceService.updateResourceAssociations(missingOperator));
    }


    // Builder methods used for creating entities
    private CourseEntity.CourseEntityBuilder dummyCourseEntityBuilder(final OffsetDateTime now, final boolean published) {
        return CourseEntity.builder()
                .id(UUID.randomUUID())
                .title("title")
                .description("description")
                .startDate(now.minusMonths(1))
                .endDate(now.plusMonths(2))
                .published(published);

    }

    private CourseEntity.CourseEntityBuilder invalidDummyCourseEntityBuilder(final OffsetDateTime now, final boolean published) {
        return CourseEntity.builder()
                .id(UUID.randomUUID())
                .title("title")
                .description("description")
                .startDate(now.plusMonths(1))
                .endDate(now.minusMonths(2))
                .published(published);

    }

    private static ChapterEntity.ChapterEntityBuilder dummyChapterEntityBuilder(final UUID courseId) {
        return ChapterEntity.builder()
                .id(UUID.randomUUID())
                .courseId(courseId)
                .title("testTitle")
                .description("testDescription")
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().plusMonths(1))
                .number(1);
    }

}