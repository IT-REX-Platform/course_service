package de.unistuttgart.iste.gits.course_service.service;

import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseMembershipEntity;
import de.unistuttgart.iste.gits.course_service.persistence.mapper.CourseMapper;
import de.unistuttgart.iste.gits.course_service.persistence.mapper.MembershipMapper;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseMembershipRepository;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.generated.dto.*;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.time.OffsetDateTime;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MembershipServiceTest {

    private final CourseMembershipRepository courseMembershipRepository = mock(CourseMembershipRepository.class);

    private final CourseRepository courseRepository = mock(CourseRepository.class);

    private final ModelMapper modelMapper = new ModelMapper();

    private final MembershipMapper membershipMapper = new MembershipMapper(modelMapper);

    private final CourseMapper courseMapper = new CourseMapper(modelMapper);

    private final MembershipService membershipService = new MembershipService(courseMembershipRepository,
            courseRepository,
            membershipMapper,
            courseMapper);

    @Test
    void getAllMembershipsByUserIdsTest() {
        // init data
        final List<CourseMembershipEntity> entities = new ArrayList<>();
        final List<CourseMembership> membershipDtos = new ArrayList<>();
        final UUID userId = UUID.randomUUID();
        final List<Course> courses = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            courses.add(Course.builder().setId(UUID.randomUUID()).setPublished(false).build());
            entities.add(CourseMembershipEntity.builder()
                    .userId(userId)
                    .courseId(courses.get(i).getId())
                    .role(UserRoleInCourse.STUDENT).build());
            membershipDtos.add(CourseMembership.builder()
                    .setUserId(userId)
                    .setCourseId(courses.get(i).getId())
                    .setRole(UserRoleInCourse.STUDENT)
                    .setCourse(courses.get(i)).build());
        }

        final List<CourseEntity> courseEntities = courses.stream()
                .map(course -> modelMapper.map(course, CourseEntity.class))
                .toList();

        //mock repository
        when(courseMembershipRepository.findByUserId(userId)).thenReturn(entities);
        when(courseRepository.getReferenceById(courses.get(0).getId())).thenReturn(courseEntities.get(0));
        when(courseRepository.getReferenceById(courses.get(1).getId())).thenReturn(courseEntities.get(1));
        when(courseRepository.getReferenceById(courses.get(2).getId())).thenReturn(courseEntities.get(2));

        // run method under test
        final List<CourseMembership> resultSet = membershipService.getAllMembershipByUserId(userId, null);

        // compare results
        assertEquals(membershipDtos.size(), resultSet.size());

        for (final CourseMembership item : resultSet) {
            assertTrue(membershipDtos.contains(item));
        }
    }

    @Test
    void getMembershipsWithFilter() {
        // init data
        final List<CourseMembershipEntity> entities = new ArrayList<>();
        final UUID userId = UUID.randomUUID();
        final List<Course> courses = new ArrayList<>();

        courses.add(Course.builder().setId(UUID.randomUUID()).setPublished(false).build());
        courses.add(Course.builder().setId(UUID.randomUUID())
                .setPublished(true)
                .setStartDate(OffsetDateTime.now().minusDays(3))
                .setEndDate(OffsetDateTime.now().minusDays(1))
                .build());
        courses.add(Course.builder().setId(UUID.randomUUID())
                .setPublished(true)
                .setStartDate(OffsetDateTime.now().plusDays(1))
                .setEndDate(OffsetDateTime.now().plusDays(3))
                .build());

        final Course onlyAvailableCourse = Course.builder()
                .setId(UUID.randomUUID())
                .setPublished(true)
                .setStartDate(OffsetDateTime.now().minusDays(3))
                .setEndDate(OffsetDateTime.now().plusDays(3))
                .build();
        courses.add(onlyAvailableCourse);

        for (int i = 0; i < 4; i++) {
            entities.add(CourseMembershipEntity.builder()
                    .userId(userId)
                    .courseId(courses.get(i).getId())
                    .role(UserRoleInCourse.STUDENT).build());
        }
        final List<CourseEntity> courseEntities = courses.stream()
                .map(course -> modelMapper.map(course, CourseEntity.class))
                .toList();

        // mock repository and mapper
        when(courseMembershipRepository.findByUserId(userId)).thenReturn(entities);
        when(courseRepository.getReferenceById(courses.get(0).getId())).thenReturn(courseEntities.get(0));
        when(courseRepository.getReferenceById(courses.get(1).getId())).thenReturn(courseEntities.get(1));
        when(courseRepository.getReferenceById(courses.get(2).getId())).thenReturn(courseEntities.get(2));
        when(courseRepository.getReferenceById(courses.get(3).getId())).thenReturn(courseEntities.get(3));

        // run method under test
        List<CourseMembership> resultSet = membershipService.getAllMembershipByUserId(userId, true);

        // compare results
        assertThat(resultSet, hasSize(1));
        assertThat(resultSet.get(0).getCourseId(), is(onlyAvailableCourse.getId()));

        // run method under test but with false as parameter
        resultSet = membershipService.getAllMembershipByUserId(userId, false);

        // compare results
        assertThat(resultSet, hasSize(3));
        assertThat(resultSet.get(0).getCourseId(), is(courses.get(0).getId()));
        assertThat(resultSet.get(1).getCourseId(), is(courses.get(1).getId()));
        assertThat(resultSet.get(2).getCourseId(), is(courses.get(2).getId()));
    }

}