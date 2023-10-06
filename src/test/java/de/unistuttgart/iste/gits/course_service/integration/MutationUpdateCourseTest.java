package de.unistuttgart.iste.gits.course_service.integration;

import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.common.testutil.MockTestPublisherConfiguration;
import de.unistuttgart.iste.gits.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.gits.course_service.persistence.entity.ChapterEntity;
import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.gits.course_service.persistence.repository.ChapterRepository;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseMembershipRepository;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.generated.dto.Chapter;
import de.unistuttgart.iste.gits.generated.dto.YearDivision;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static de.unistuttgart.iste.gits.common.testutil.HeaderUtils.addCurrentUserHeader;
import static de.unistuttgart.iste.gits.common.testutil.TestUsers.userWithMembershipInCourseWithId;
import static de.unistuttgart.iste.gits.course_service.test_utils.TestUtils.saveCourseMembershipsOfUserToRepository;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the `updateCourse` mutation.
 */
@ContextConfiguration(classes = MockTestPublisherConfiguration.class)
@GraphQlApiTest
class MutationUpdateCourseTest {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private CourseMembershipRepository courseMembershipRepository;

    /**
     * Given a valid UpdateCourseInput
     * When the updateCourse mutation is executed
     * Then the course is updated and returned
     */
    @Test
    @Transactional
    void testUpdateCourseSuccessful(HttpGraphQlTester tester) {
        // create a course with a chapter in the database
        final CourseEntity initialCourse = courseRepository.save(CourseEntity.builder().title("Course 1")
                .description("This is course 1")
                .startDate(OffsetDateTime.parse("2020-01-01T00:00:00.000Z"))
                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"))
                .startYear(2021)
                .yearDivision(YearDivision.FIRST_SEMESTER)
                .published(true)
                .build());

        // create admin user object
        final LoggedInUser adminUser = userWithMembershipInCourseWithId(initialCourse.getId(),
                LoggedInUser.UserRoleInCourse.ADMINISTRATOR);
        // save course memberships of admin to repository
        saveCourseMembershipsOfUserToRepository(courseMembershipRepository, adminUser);

        // add admin user data to header
        tester = addCurrentUserHeader(tester, adminUser);

        chapterRepository.save(ChapterEntity.builder()
                .courseId(initialCourse.getId())
                .title("Chapter 1")
                .description("This is chapter 1")
                .startDate(OffsetDateTime.parse("2020-01-01T00:00:00.000Z"))
                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"))
                .number(1)
                .build());

        final String query = """
                mutation {
                    updateCourse(
                        input: {
                            id: "%s"
                            title: "New Course"
                            description: "This is a new course"
                            startDate: "2000-01-01T00:00:00.000Z"
                            endDate: "2001-01-01T00:00:00.000Z"
                            startYear: 2021
                            yearDivision: FIRST_SEMESTER
                            published: false
                        }
                    ) {
                        id
                        title
                        description
                        startDate
                        endDate
                        startYear
                        yearDivision
                        published
                        chapters {
                            elements {
                                id
                            }
                        }
                    }
                }""".formatted(initialCourse.getId());

        tester.document(query)
                .execute()
                .path("updateCourse.id").entity(String.class).isEqualTo(initialCourse.getId().toString())
                .path("updateCourse.title").entity(String.class).isEqualTo("New Course")
                .path("updateCourse.description").entity(String.class).isEqualTo("This is a new course")
                .path("updateCourse.startDate").entity(String.class).isEqualTo("2000-01-01T00:00:00.000Z")
                .path("updateCourse.endDate").entity(String.class).isEqualTo("2001-01-01T00:00:00.000Z")
                .path("updateCourse.startYear").entity(Integer.class).isEqualTo(2021)
                .path("updateCourse.yearDivision").entity(YearDivision.class).isEqualTo(YearDivision.FIRST_SEMESTER)
                .path("updateCourse.published").entity(Boolean.class).isEqualTo(false)
                .path("updateCourse.chapters.elements").entityList(Chapter.class).hasSize(1);

        // check that the course was updated in the database
        final CourseEntity updatedCourse = courseRepository.findById(initialCourse.getId()).orElseThrow();
        assertThat(updatedCourse.getTitle(), is("New Course"));
        assertThat(updatedCourse.getDescription(), is("This is a new course"));
        assertThat(updatedCourse.isPublished(), is(false));
        assertThat(updatedCourse.getStartDate().isEqual(OffsetDateTime.parse("2000-01-01T00:00:00.000Z")), is(true));
        assertThat(updatedCourse.getEndDate().isEqual(OffsetDateTime.parse("2001-01-01T00:00:00.000Z")), is(true));

        // check that the chapter was not deleted
        final List<ChapterEntity> chapters = chapterRepository.findAll();
        assertThat(chapters.size(), is(1));
        // assertThat(chapters.get(0).getCourse().getId(), is(initialData.getId()));
    }

    /**
     * Given a UpdateCourseInput with a non-existing id
     * When the createCourse mutation is executed
     * Then an error is returned
     */
    @Test
    void testUpdateCourseNotExisting(HttpGraphQlTester tester) {
        final UUID courseId = UUID.randomUUID();

        // create admin user object
        final LoggedInUser adminUser = userWithMembershipInCourseWithId(courseId,
                LoggedInUser.UserRoleInCourse.ADMINISTRATOR);
        // save course memberships of admin to repository
        saveCourseMembershipsOfUserToRepository(courseMembershipRepository, adminUser);

        // add admin user data to header
        tester = addCurrentUserHeader(tester, adminUser);

        final String query = String.format("""
                mutation {
                    updateCourse(
                        input: {
                            id: "%s"
                            title: "New Course"
                            description: "This is a new course"
                            startDate: "2000-01-01T00:00:00.000Z"
                            endDate: "2001-01-01T00:00:00.000Z"
                            published: false
                        }
                    ) {
                        id
                    }
                }""", courseId);

        tester.document(query)
                .execute()
                .errors()
                .expect(responseError -> requireNonNull(responseError.getMessage())
                        .contains("Course with id " + courseId + " not found"));
    }

    /**
     * Given a UpdateCourseInput with a blank title
     * When the createCourse mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testErrorOnBlankTitle(final GraphQlTester tester) {
        final String query = """
                mutation {
                    updateCourse(
                        input: {
                            id: "00000000-0000-0000-0000-000000000000"
                            title: " "
                            description: "This is a new course"
                            startDate: "2020-01-01T00:00:00.000Z"
                            endDate: "2021-01-01T00:00:00.000Z"
                            published: false
                        }
                    ) {
                        id
                        title
                    }
                }""";

        tester.document(query)
                .execute()
                .errors()
                .expect(responseError -> responseError.getMessage() != null
                                         && responseError.getMessage().contains("must not be blank"));
    }

    /**
     * Given a UpdateCourseInput with a title that is too long
     * When the createCourse mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testTooLongTitle(final GraphQlTester tester) {
        final String query = String.format("""
                mutation {
                    updateCourse(
                        input: {
                            id: "00000000-0000-0000-0000-000000000000"
                            title: "%s"
                            description: "This is a new course"
                            startDate: "2020-01-01T00:00:00.000Z"
                            endDate: "2021-01-01T00:00:00.000Z"
                            published: false
                        }
                    ) {
                        id
                        title
                    }
                }""", "a".repeat(256));

        tester.document(query)
                .execute()
                .errors()
                .expect(responseError -> responseError.getMessage() != null
                                         && responseError.getMessage().contains("size must be between 0 and 255"));
    }

    /**
     * Given a UpdateCourseInput with a too long description
     * When the createCourse mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testTooLongDescription(final GraphQlTester tester) {
        final String query = String.format("""
                mutation {
                    updateCourse(
                        input: {
                            id: "00000000-0000-0000-0000-000000000000"
                            title: "New Course"
                            description: "%s"
                            startDate: "2020-01-01T00:00:00.000Z"
                            endDate: "2021-01-01T00:00:00.000Z"
                            published: false
                        }
                    ) {
                        id
                        title
                    }
                }""", "a".repeat(3001));

        tester.document(query)
                .execute()
                .errors()
                .expect(responseError -> responseError.getMessage() != null
                                         && responseError.getMessage().contains("size must be between 0 and 3000"));
    }

    /**
     * Given an UpdateCourseInput where the start date is after the end date
     * When the createCourse mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testStartDateAfterEndDate(HttpGraphQlTester tester) {
        final UUID courseId = UUID.randomUUID();

        // create admin user object
        final LoggedInUser adminUser = userWithMembershipInCourseWithId(courseId,
                LoggedInUser.UserRoleInCourse.ADMINISTRATOR);
        // save course memberships of admin to repository
        saveCourseMembershipsOfUserToRepository(courseMembershipRepository, adminUser);

        // add admin user data to header
        tester = addCurrentUserHeader(tester, adminUser);

        final String query = """
                mutation {
                    updateCourse(
                        input: {
                            id: "%s"
                            title: "New Course"
                            description: "This is a new course"
                            startDate: "2021-01-01T00:00:00.000Z"
                            endDate: "2020-01-01T00:00:00.000Z"
                            published: false
                        }
                    ) {
                        id
                        title
                    }
                }""".formatted(courseId);

        tester.document(query)
                .execute()
                .errors()
                .expect(responseError -> responseError.getMessage() != null
                                         && responseError.getMessage()
                                                 .toLowerCase().contains("start date must be before end date"));
    }
}
