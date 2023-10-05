package de.unistuttgart.iste.gits.course_service.test_utils;

import de.unistuttgart.iste.gits.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseMembershipEntity;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseMembershipRepository;
import de.unistuttgart.iste.gits.generated.dto.UserRoleInCourse;

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
        for(LoggedInUser.CourseMembership membership : user.getCourseMemberships()) {
            courseMembershipRepository.save(CourseMembershipEntity.builder()
                    .courseId(membership.getCourseId())
                    .userId(user.getId())
                    .role(UserRoleInCourse.valueOf(membership.getRole().toString()))
                    .build());
        }
    }
}
