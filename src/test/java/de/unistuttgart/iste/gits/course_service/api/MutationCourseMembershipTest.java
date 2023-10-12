package de.unistuttgart.iste.gits.course_service.api;

import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseMembershipEntity;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseMembershipRepository;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.generated.dto.CourseMembership;
import de.unistuttgart.iste.gits.generated.dto.UserRoleInCourse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.HttpGraphQlTester;

import java.util.UUID;

import static de.unistuttgart.iste.gits.common.testutil.HeaderUtils.addCurrentUserHeader;
import static de.unistuttgart.iste.gits.common.testutil.TestUsers.userWithMembershipInCourseWithId;
import static de.unistuttgart.iste.gits.course_service.test_utils.TestUtils.dummyCourseBuilder;
import static de.unistuttgart.iste.gits.course_service.test_utils.TestUtils.saveCourseMembershipsOfUserToRepository;

@GraphQlApiTest
class MutationCourseMembershipTest {

    @Autowired
    private CourseMembershipRepository courseMembershipRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    void createMembershipTest(HttpGraphQlTester tester) {
        // create course
        final CourseEntity course = courseRepository.save(dummyCourseBuilder().build());

        // create admin user object
        final LoggedInUser adminUser = userWithMembershipInCourseWithId(course.getId(),
                LoggedInUser.UserRoleInCourse.ADMINISTRATOR);
        // save course memberships of admin to repository
        saveCourseMembershipsOfUserToRepository(courseMembershipRepository, adminUser);

        // add admin user data to header
        tester = addCurrentUserHeader(tester, adminUser);

        final CourseMembership expectedDto = CourseMembership.builder()
                .setUserId(UUID.randomUUID())
                .setCourseId(course.getId())
                .setRole(UserRoleInCourse.STUDENT)
                .build();

        final String query = """
                mutation {
                    createMembership(
                        input: {
                            userId: "%s"
                            courseId: "%s"
                            role: %s
                        }
                    ) {
                        userId
                        courseId
                        role
                    }
                }
                """.formatted(expectedDto.getUserId(), expectedDto.getCourseId(), expectedDto.getRole());

        tester.document(query)
                .execute()
                .path("createMembership")
                .entity(CourseMembership.class)
                .isEqualTo(expectedDto);
    }

    @Test
    void updateMembershipMembershipNotExistingTest(HttpGraphQlTester tester) {
        final UUID courseId = UUID.randomUUID();

        // create admin user object
        final LoggedInUser adminUser = userWithMembershipInCourseWithId(courseId,
                LoggedInUser.UserRoleInCourse.ADMINISTRATOR);
        // save course memberships of admin to repository
        saveCourseMembershipsOfUserToRepository(courseMembershipRepository, adminUser);

        // add admin user data to header
        tester = addCurrentUserHeader(tester, adminUser);

        final UUID userIdToTest = UUID.randomUUID();

        final CourseMembership expectedDto = CourseMembership.builder()
                .setUserId(userIdToTest)
                .setCourseId(courseId)
                .setRole(UserRoleInCourse.STUDENT)
                .build();

        final String query = """
                mutation {
                    updateMembership(
                        input: {
                            userId: "%s"
                            courseId: "%s"
                            role: %s
                        }
                    ) {
                        userId
                        courseId
                        role
                    }
                }
                """.formatted(expectedDto.getUserId(), expectedDto.getCourseId(), expectedDto.getRole());

        tester.document(query)
                .execute()
                .errors()
                .expect(responseError -> responseError.getMessage() != null && responseError.getMessage().contains("not member in course"));
    }

    @Test
    void updateMembershipTest(HttpGraphQlTester tester) {
        final UUID courseId = UUID.randomUUID();

        // create admin user object
        final LoggedInUser adminUser = userWithMembershipInCourseWithId(courseId,
                LoggedInUser.UserRoleInCourse.ADMINISTRATOR);
        // save course memberships of admin to repository
        saveCourseMembershipsOfUserToRepository(courseMembershipRepository, adminUser);

        // add admin user data to header
        tester = addCurrentUserHeader(tester, adminUser);

        //init input data
        final UUID userId = UUID.randomUUID();

        final CourseMembershipEntity entity = CourseMembershipEntity.builder()
                .userId(userId)
                .courseId(courseId)
                .role(UserRoleInCourse.STUDENT)
                .build();
        final CourseMembership expectedDto = CourseMembership.builder()
                .setUserId(userId)
                .setCourseId(courseId)
                .setRole(UserRoleInCourse.STUDENT)
                .build();

        //create entity in DB
        courseMembershipRepository.save(entity);

        final String query = """
                mutation {
                    updateMembership(
                        input: {
                            userId: "%s"
                            courseId: "%s"
                            role: %s
                        }
                    ) {
                        userId
                        courseId
                        role
                    }
                }
                """.formatted(expectedDto.getUserId(), expectedDto.getCourseId(), expectedDto.getRole());

        tester.document(query)
                .execute()
                .path("updateMembership")
                .entity(CourseMembership.class)
                .isEqualTo(expectedDto);
    }

    @Test
    void deleteNotExistingMembershipTest(HttpGraphQlTester tester) {
        final UUID courseId = UUID.randomUUID();

        // create admin user object
        final LoggedInUser adminUser = userWithMembershipInCourseWithId(courseId,
                LoggedInUser.UserRoleInCourse.ADMINISTRATOR);
        // save course memberships of admin to repository
        saveCourseMembershipsOfUserToRepository(courseMembershipRepository, adminUser);

        // add admin user data to header
        tester = addCurrentUserHeader(tester, adminUser);

        //init input data
        final UUID userId = UUID.randomUUID();

        final CourseMembership expectedDto = CourseMembership.builder()
                .setUserId(userId)
                .setCourseId(courseId)
                .setRole(UserRoleInCourse.STUDENT)
                .build();


        final String query = """
                mutation {
                    deleteMembership(
                        input: {
                            userId: "%s"
                            courseId: "%s"
                            role: %s
                        }
                    ) {
                        userId
                        courseId
                        role
                    }
                }
                """.formatted(expectedDto.getUserId(), expectedDto.getCourseId(), expectedDto.getRole());

        tester.document(query)
                .execute()
                .errors()
                .expect(responseError -> responseError.getMessage() != null && responseError.getMessage().contains("not member in course"));
    }

    @Test
    void deleteMembershipTest(HttpGraphQlTester tester) {
        final UUID courseId = UUID.randomUUID();

        // create admin user object
        final LoggedInUser adminUser = userWithMembershipInCourseWithId(courseId,
                LoggedInUser.UserRoleInCourse.ADMINISTRATOR);
        // save course memberships of admin to repository
        saveCourseMembershipsOfUserToRepository(courseMembershipRepository, adminUser);

        // add admin user data to header
        tester = addCurrentUserHeader(tester, adminUser);

        //init input data
        final UUID userId = UUID.randomUUID();

        final CourseMembershipEntity entity = CourseMembershipEntity.builder()
                .userId(userId)
                .courseId(courseId)
                .role(UserRoleInCourse.STUDENT).build();
        final CourseMembership expectedDto = CourseMembership.builder()
                .setUserId(userId)
                .setCourseId(courseId)
                .setRole(UserRoleInCourse.STUDENT)
                .build();

        //create entity in DB
        courseMembershipRepository.save(entity);

        final String query = """
                mutation {
                    deleteMembership(
                        input: {
                            userId: "%s"
                            courseId: "%s"
                            role: %s
                        }
                    ) {
                        userId
                        courseId
                        role
                    }
                }
                """.formatted(expectedDto.getUserId(), expectedDto.getCourseId(), expectedDto.getRole());

        tester.document(query)
                .execute()
                .path("deleteMembership")
                .entity(CourseMembership.class)
                .isEqualTo(expectedDto);
    }

}
