package de.unistuttgart.iste.gits.course_service.api;

import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.common.testutil.MockTestPublisherConfiguration;
import de.unistuttgart.iste.gits.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.gits.common.user_handling.LoggedInUser.UserRoleInCourse;
import de.unistuttgart.iste.gits.course_service.persistence.entity.ChapterEntity;
import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.gits.course_service.persistence.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static de.unistuttgart.iste.gits.common.testutil.HeaderUtils.addCurrentUserHeader;
import static de.unistuttgart.iste.gits.common.testutil.TestUsers.userWithMembershipInCourseWithId;
import static de.unistuttgart.iste.gits.course_service.test_utils.TestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
/**
 * Tests for the `deleteCourse` mutation.
 */
@ContextConfiguration(classes = MockTestPublisherConfiguration.class)
@GraphQlApiTest
class MutationDeleteCourseTest {

    @Autowired
    private CourseMembershipRepository courseMembershipRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ChapterRepository chapterRepository;

    /**
     * Given a valid course id
     * When the deleteCourse mutation is executed
     * Then the course is deleted and the uuid is returned
     */
    @Test
    void testDeletion(HttpGraphQlTester tester) {
        // create two courses in the database
        final List<CourseEntity> initialCourses = Stream.of(
                        dummyCourseBuilder().title("Course 1").build(),
                        dummyCourseBuilder().title("Course 2").build())
                .map(courseRepository::save)
                .toList();

        // create admin user object
        final LoggedInUser adminUser = userWithMembershipInCourseWithId(initialCourses.get(0).getId(),
                UserRoleInCourse.ADMINISTRATOR);
        // save course memberships of admin to repository
        saveCourseMembershipsOfUserToRepository(courseMembershipRepository, adminUser);

        // add admin user data to header
        tester = addCurrentUserHeader(tester, adminUser);

        // create a chapter in the database to check that it is deleted
        chapterRepository.save(dummyChapterBuilder().courseId(initialCourses.get(0).getId()).build());

        final String query = """
                mutation {
                    deleteCourse(id: "%s")
                }""".formatted(initialCourses.get(0).getId());

        tester.document(query)
                .execute()
                .path("deleteCourse").entity(UUID.class).isEqualTo(initialCourses.get(0).getId());

        final var entities = courseRepository.findAll();
        assertThat(entities, hasSize(1));
        // check that the correct course was deleted and the other one is still there
        assertThat(entities.get(0).getId(), equalTo(initialCourses.get(1).getId()));
        // check that the chapter was deleted
        assertThat(chapterRepository.findAll(), hasSize(0));
    }

    /**
     * Given an invalid course id
     * When the deleteCourse mutation is executed
     * Then an error is returned
     */
    @Test
    void testDeletionInvalidId(HttpGraphQlTester tester) {
        final UUID courseId = UUID.randomUUID();

        // create admin user object
        final LoggedInUser adminUser = userWithMembershipInCourseWithId(courseId, UserRoleInCourse.ADMINISTRATOR);
        // save course memberships of admin to repository
        saveCourseMembershipsOfUserToRepository(courseMembershipRepository, adminUser);

        // add admin user data to header
        tester = addCurrentUserHeader(tester, adminUser);

        final String query = """
                mutation {
                    deleteCourse(id: "%s")
                }""".formatted(courseId);

        tester.document(query)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors, hasSize(1));
                    assertThat(responseErrors.get(0).getMessage(), containsString("Course with id"));
                    assertThat(responseErrors.get(0).getMessage(), containsString("not found"));
                });
    }



}
