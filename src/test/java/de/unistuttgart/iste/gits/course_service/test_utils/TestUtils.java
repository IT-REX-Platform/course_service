package de.unistuttgart.iste.gits.course_service.test_utils;

import de.unistuttgart.iste.gits.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.gits.course_service.persistence.entity.ChapterEntity;
import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseMembershipEntity;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseMembershipRepository;
import de.unistuttgart.iste.gits.generated.dto.UserRoleInCourse;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Class containing helper methods for tests specific to this service.
 */
public class TestUtils {
    /**
     * Helper method to save all course memberships of a user to the database.
     * @param user The user whose course memberships should be saved.
     */
    public static void saveCourseMembershipsOfUserToRepository(final CourseMembershipRepository courseMembershipRepository,
                                                         final LoggedInUser user) {
        for(final LoggedInUser.CourseMembership membership : user.getCourseMemberships()) {
            courseMembershipRepository.save(CourseMembershipEntity.builder()
                    .courseId(membership.getCourseId())
                    .userId(user.getId())
                    .role(UserRoleInCourse.valueOf(membership.getRole().toString()))
                    .build());
        }
    }

    /**
     * Helper method to create a course builder and initialize it with some dummy data.
     */
    public static CourseEntity.CourseEntityBuilder dummyCourseBuilder() {
        return CourseEntity.builder()
                .id(UUID.randomUUID())
                .title("Course 1")
                .description("This is course 1")
                .published(false)
                .startDate(OffsetDateTime.parse("2020-01-01T00:00:00.000Z"))
                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"));
    }
    public static ChapterEntity.ChapterEntityBuilder dummyChapterBuilder() {
        return ChapterEntity.builder()
                .courseId(UUID.randomUUID())
                .title("Chapter 1")
                .description("This is chapter 1")
                .number(1)
                .startDate(OffsetDateTime.parse("2020-01-01T00:00:00.000Z"))
                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"));
    }
}
