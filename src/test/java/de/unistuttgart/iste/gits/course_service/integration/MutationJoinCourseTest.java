package de.unistuttgart.iste.gits.course_service.integration;

import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.common.testutil.InjectCurrentUserHeader;
import de.unistuttgart.iste.gits.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseMembershipEntity;
import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseMembershipPk;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseMembershipRepository;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.generated.dto.UserRoleInCourse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.HttpGraphQlTester;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.assertj.core.api.Assertions.*;

@GraphQlApiTest
class MutationJoinCourseTest {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CourseMembershipRepository courseMembershipRepository;

    @InjectCurrentUserHeader
    private final UUID currentUserId = UUID.randomUUID();

    @Test
    void testJoinCourse(final HttpGraphQlTester tester) {
        final CourseEntity course = courseRepository.save(CourseEntity.builder().title("Course 1")
                .description("This is course 1")
                .startDate(OffsetDateTime.parse("2020-01-01T00:00:00.000Z"))
                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"))
                .published(true)
                .build());

        final String query =
                """
                mutation($courseId: UUID!) {
                    joinCourse(courseId: $courseId) {
                        userId
                        courseId
                        role
                        course {
                            description
                        }
                    }
                }
                """;

        tester.document(query)
                .variable("courseId", course.getId())
                .execute()
                .path("joinCourse.userId").entity(UUID.class).isEqualTo(currentUserId)
                .path("joinCourse.courseId").entity(UUID.class).isEqualTo(course.getId())
                .path("joinCourse.role").entity(UserRoleInCourse.class).isEqualTo(UserRoleInCourse.STUDENT)
                .path("joinCourse.course.description").entity(String.class).isEqualTo("This is course 1");

        final CourseMembershipEntity membership = assertDoesNotThrow(() ->
                courseMembershipRepository
                        .findById(new CourseMembershipPk(currentUserId, course.getId()))
                        .orElseThrow());

        assertThat(membership.getUserId()).isEqualTo(currentUserId);
        assertThat(membership.getCourseId()).isEqualTo(course.getId());
        assertThat(membership.getRole()).isEqualTo(UserRoleInCourse.STUDENT);
    }
}
