package de.unistuttgart.iste.gits.course_service.integration;

import de.unistuttgart.iste.gits.common.exception.NoAccessToCourseException;
import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseMembershipEntity;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseMembershipRepository;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.generated.dto.Course;
import de.unistuttgart.iste.gits.generated.dto.CourseMembership;
import de.unistuttgart.iste.gits.generated.dto.UserRoleInCourse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static de.unistuttgart.iste.gits.common.testutil.HeaderUtils.addCurrentUserHeader;
import static de.unistuttgart.iste.gits.common.testutil.TestUsers.userWithMembershipInCourseWithId;

@GraphQlApiTest
class QueryCourseMembershipsTest {

    @Autowired
    private CourseMembershipRepository membershipRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    void testNoMembershipExisting(final GraphQlTester tester){
        final List<UUID> userIds = List.of(UUID.randomUUID());
        final String query = """
                query($userIds: [UUID!]!) {
                        _internal_noauth_courseMembershipsByUserIds(userIds: $userIds) {
                            userId
                            courseId
                            role
                    }
                }
                """;
        tester.document(query)
                .variable("userIds", userIds.subList(0,1))
                .execute()
                .path("_internal_noauth_courseMembershipsByUserIds[*][*]")
                .entityList(CourseMembership.class)
                .hasSize(0);
    }

    @Test
    void testMembership(final GraphQlTester tester){

        final UUID userId = UUID.randomUUID();
        final List<CourseMembership> courseMemberships = new ArrayList<>();
        final List<UUID> userIds = List.of(userId);
        final List<CourseEntity> courseEntities = List.of(createTestCourse(), createTestCourse());
        courseRepository.saveAll(courseEntities);
        final Course course = createTestCourseDto();

        for (int i = 0; i < 2; i++) {
            final UUID courseId = courseEntities.get(i).getId();
            final CourseMembershipEntity entity = CourseMembershipEntity.builder()
                    .userId(userId)
                    .courseId(courseId)
                    .role(UserRoleInCourse.TUTOR)
                    .build();
            final CourseMembership dto = CourseMembership.builder()
                    .setUserId(userId)
                    .setCourseId(courseId)
                    .setRole(UserRoleInCourse.TUTOR)
                    .setCourse(course)
                    .build();
            membershipRepository.save(entity);
            courseMemberships.add(dto);
        }

        final String query = """
                query($userIds: [UUID!]!) {
                        _internal_noauth_courseMembershipsByUserIds(userIds: $userIds) {
                            userId
                            courseId
                            role
                            course {
                                startDate
                                endDate
                                title
                                description
                            }
                    }
                }
                """;
        tester.document(query)
                .variable("userIds", userIds.subList(0,1))
                .execute()
                .path("_internal_noauth_courseMembershipsByUserIds[*][*]")
                .entityList(CourseMembership.class)
                .hasSize(2)
                .contains(courseMemberships.get(0), courseMemberships.get(1));
    }

    @Test
    void testMembershipsFieldInCourse(HttpGraphQlTester tester) {
        final CourseEntity course = courseRepository.save(createTestCourse());

        final LoggedInUser currentUser = userWithMembershipInCourseWithId(course.getId(),
                LoggedInUser.UserRoleInCourse.ADMINISTRATOR);

        tester = addCurrentUserHeader(tester, currentUser);

        membershipRepository.save(CourseMembershipEntity.builder()
                .userId(currentUser.getId())
                .courseId(course.getId())
                .role(UserRoleInCourse.ADMINISTRATOR)
                .build());

        final String query =
                """
                query($courseId: UUID!) {
                    coursesByIds(ids: [$courseId]) {
                        memberships {
                            userId
                            courseId
                            role
                        }
                    }
                }
                """;

        tester.document(query)
                .variable("courseId", course.getId())
                .execute()
                .path("coursesByIds[0].memberships").entityList(CourseMembership.class).hasSize(1)
                .path("coursesByIds[0].memberships[0].userId").entity(UUID.class).isEqualTo(currentUser.getId())
                .path("coursesByIds[0].memberships[0].courseId").entity(UUID.class).isEqualTo(course.getId())
                .path("coursesByIds[0].memberships[0].role").entity(UserRoleInCourse.class).isEqualTo(UserRoleInCourse.ADMINISTRATOR);
    }

    @Test
    void testMembershipsFieldInCourseNoPermission(HttpGraphQlTester tester) {
        final CourseEntity course = courseRepository.save(createTestCourse());

        final LoggedInUser currentUser = userWithMembershipInCourseWithId(course.getId(),
                LoggedInUser.UserRoleInCourse.STUDENT);

        tester = addCurrentUserHeader(tester, currentUser);

        membershipRepository.save(CourseMembershipEntity.builder()
                .userId(currentUser.getId())
                .courseId(course.getId())
                .role(UserRoleInCourse.STUDENT)
                .build());

        final String query =
                """
                query($courseId: UUID!) {
                    coursesByIds(ids: [$courseId]) {
                        memberships {
                            userId
                            courseId
                            role
                        }
                    }
                }
                """;

        tester.document(query)
                .variable("courseId", course.getId())
                .execute().errors().expect(e ->
                        e.getExtensions().get("exception").equals(NoAccessToCourseException.class.getSimpleName()));
    }

    private static Course createTestCourseDto() {
        return Course.builder()
                .setStartDate(OffsetDateTime.parse("2021-01-01T00:00:00+00:00"))
                .setEndDate(OffsetDateTime.parse("2021-01-01T00:00:00+00:00"))
                .setTitle("Test Course")
                .setDescription("Test Description")
                .build();
    }

    private static CourseEntity createTestCourse() {
        return CourseEntity.builder()
                .startDate(OffsetDateTime.parse("2021-01-01T00:00:00+00:00"))
                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00+00:00"))
                .title("Test Course")
                .description("Test Description")
                .chapters(new ArrayList<>())
                .build();
    }

}
