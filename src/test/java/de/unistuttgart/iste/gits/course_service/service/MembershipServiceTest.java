package de.unistuttgart.iste.gits.course_service.service;

import de.unistuttgart.iste.gits.common.event.CourseChangeEvent;
import de.unistuttgart.iste.gits.common.event.CrudOperation;
import de.unistuttgart.iste.gits.common.exception.IncompleteEventMessageException;
import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseMembershipEntity;
import de.unistuttgart.iste.gits.course_service.persistence.mapper.MembershipMapper;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseMembershipRepository;
import de.unistuttgart.iste.gits.generated.dto.CourseMembership;
import de.unistuttgart.iste.gits.generated.dto.UserRoleInCourse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MembershipServiceTest {

    private final CourseMembershipRepository courseMembershipRepository = Mockito.mock(CourseMembershipRepository.class);

    private final MembershipMapper membershipMapper = new MembershipMapper(new ModelMapper());

    private final MembershipService membershipService = new MembershipService(courseMembershipRepository, membershipMapper);

    @Test
    void getAllMembershipsByUserTest() {
        // init data
        final List<CourseMembershipEntity> entities = new ArrayList<>();
        final List<CourseMembership> membershipDtos = new ArrayList<>();
        final UUID userId = UUID.randomUUID();

        for (int i=0; i<3; i++){
            final UUID courseId = UUID.randomUUID();
            entities.add(CourseMembershipEntity.builder().userId(userId).courseId(courseId).role(UserRoleInCourse.STUDENT).build());
            membershipDtos.add(CourseMembership.builder().setUserId(userId).setCourseId(courseId).setRole(UserRoleInCourse.STUDENT).build());
        }

        //mock repository
        when(courseMembershipRepository.findCourseMembershipEntitiesByUserIdOrderByCourseId(userId)).thenReturn(entities);

        // run method under test
        final List<CourseMembership> resultSet = membershipService.getAllMembershipsByUser(userId);


        // compare results
        assertEquals(membershipDtos.size(), resultSet.size());

        for (final CourseMembership item: resultSet) {
            assertTrue(membershipDtos.contains(item), item.toString());
        }

    }

    @Test
    void removeMembershipEventTest(){
        final UUID courseId = UUID.randomUUID();
        final CourseChangeEvent courseDeletionEvent = CourseChangeEvent.builder().courseId(courseId).operation(CrudOperation.DELETE).build();

        final List<CourseMembershipEntity> memberships = List.of(CourseMembershipEntity.builder()
                        .courseId(courseId)
                        .userId(UUID.randomUUID())
                        .role(UserRoleInCourse.STUDENT)
                        .build(),
                CourseMembershipEntity.builder()
                        .courseId(courseId)
                        .userId(UUID.randomUUID())
                        .role(UserRoleInCourse.ADMINISTRATOR)
                        .build()
                );

        //mock repository
        when(courseMembershipRepository.findCourseMembershipEntitiesByCourseId(courseId)).thenReturn(memberships);

        //execute method under test
        assertDoesNotThrow( () -> membershipService.removeCourse(courseDeletionEvent));

        //verify called methods
        verify(courseMembershipRepository).deleteAll(memberships);

    }

    @Test
    void noRemoveMembershipEventTest(){
        final CourseChangeEvent courseCreateEvent = CourseChangeEvent.builder().courseId(UUID.randomUUID()).operation(CrudOperation.CREATE).build();
        final CourseChangeEvent courseUpdateEvent = CourseChangeEvent.builder().courseId(UUID.randomUUID()).operation(CrudOperation.UPDATE).build();

        //execute method under test
        assertDoesNotThrow( () -> membershipService.removeCourse(courseCreateEvent));
        assertDoesNotThrow( () -> membershipService.removeCourse(courseUpdateEvent));

        //verify called methods
        verify(courseMembershipRepository, never()).deleteAll(any());
    }
    @Test
    void incompleteCourseEventTest(){
        final CourseChangeEvent courseEventNoId = CourseChangeEvent.builder().operation(CrudOperation.CREATE).build();
        final CourseChangeEvent courseEventNoOperation = CourseChangeEvent.builder().courseId(UUID.randomUUID()).build();

        //execute method under test
        assertThrows(IncompleteEventMessageException.class, () -> membershipService.removeCourse(courseEventNoId));
        assertThrows(IncompleteEventMessageException.class, () -> membershipService.removeCourse(courseEventNoOperation));
    }
}