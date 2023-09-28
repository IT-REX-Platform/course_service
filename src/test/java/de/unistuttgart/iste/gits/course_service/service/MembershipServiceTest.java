package de.unistuttgart.iste.gits.course_service.service;

import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseMembershipEntity;
import de.unistuttgart.iste.gits.course_service.persistence.mapper.MembershipMapper;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseMembershipRepository;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.generated.dto.Course;
import de.unistuttgart.iste.gits.generated.dto.CourseMembership;
import de.unistuttgart.iste.gits.generated.dto.UserRoleInCourse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class MembershipServiceTest {

    private final CourseMembershipRepository courseMembershipRepository = Mockito.mock(CourseMembershipRepository.class);

    private final CourseRepository courseRepository = Mockito.mock(CourseRepository.class);

    private final MembershipMapper membershipMapper = new MembershipMapper(new ModelMapper());

    private final MembershipService membershipService = new MembershipService(courseMembershipRepository, courseRepository, membershipMapper);

    @Test
    void getAllMembershipsByUserIdsTest() {
        // init data
        final List<CourseMembershipEntity> entities = new ArrayList<>();
        final List<CourseMembership> membershipDtos = new ArrayList<>();
        final List<UUID> userId = List.of(UUID.randomUUID());
        final List<Course> courses = new ArrayList<>();

        for (int i=0; i<3; i++){
            courses.add(Course.builder().setId(UUID.randomUUID()).setPublished(false).build());
            entities.add(CourseMembershipEntity.builder().userId(userId.get(0)).courseId(courses.get(i).getId()).role(UserRoleInCourse.STUDENT).build());
            membershipDtos.add(CourseMembership.builder().setUserId(userId.get(0)).setCourseId(courses.get(i).getId()).setRole(UserRoleInCourse.STUDENT).setCourse(courses.get(i)).build());
        }

        //mock repository
        when(courseMembershipRepository.findByUserIdIn(userId)).thenReturn(entities);

        // run method under test
        final List<List<CourseMembership>> resultSet = membershipService.getAllMembershipsByUserIds(userId);


        // compare results
        assertEquals(membershipDtos.size(), resultSet.get(0).size());

        for (final CourseMembership item: resultSet.get(0)) {
            assertTrue(membershipDtos.contains(item), item.toString());
        }

    }

}