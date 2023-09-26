package de.unistuttgart.iste.gits.course_service.integration;

import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.course_service.persistence.repository.ChapterRepository;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.course_service.test_config.MockTopicPublisherConfiguration;
import de.unistuttgart.iste.gits.generated.dto.YearDivision;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.ContextConfiguration;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the `createCourse` mutation.
 */
@ContextConfiguration(classes = MockTopicPublisherConfiguration.class)
@GraphQlApiTest
class MutationCreateCourseTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    /**
     * Given a valid CreateCourseInput
     * When the createCourse mutation is executed
     * Then the course is created and returned
     */
    @Test
    void testCreateCourse(final HttpGraphQlTester tester) {
        final String currentUser = getCurrentUser();

        final String query = """
                mutation {
                    createCourse(
                        input: {
                            title: "New Course"
                            description: "This is a new course"
                            startDate: "2020-01-01T00:00:00.000Z"
                            endDate: "2021-01-01T00:00:00.000Z"
                            published: false
                        }
                    ) {
                        id
                        title
                        description
                        startDate
                        endDate
                        published
                        chapters {
                            elements {
                                id
                            }
                        }
                    }
                }""";

        final UUID id = tester
                .mutate()
                .header("CurrentUser", currentUser)
                .build()
                .document(query)
                .execute()
                .path("createCourse.title").entity(String.class).isEqualTo("New Course")
                .path("createCourse.description").entity(String.class).isEqualTo("This is a new course")
                .path("createCourse.startDate").entity(String.class).isEqualTo("2020-01-01T00:00:00.000Z")
                .path("createCourse.endDate").entity(String.class).isEqualTo("2021-01-01T00:00:00.000Z")
                .path("createCourse.chapters.elements").entityList(String.class).hasSize(0)
                .path("createCourse.published").entity(Boolean.class).isEqualTo(false)
                .path("createCourse.id").entity(UUID.class).get();

        // check that the course was created in the database
        assertThat(courseRepository.count(), is(1L));
        final var course = courseRepository.findAll().get(0);
        assertThat(course.getId(), is(id));
        assertThat(course.getTitle(), is("New Course"));
        assertThat(course.getDescription(), is("This is a new course"));
        assertThat(course.isPublished(), is(false));
        assertThat(course.getStartDate().isEqual(OffsetDateTime.parse("2020-01-01T00:00:00.000Z")), is(true));
        assertThat(course.getEndDate().isEqual(OffsetDateTime.parse("2021-01-01T00:00:00.000Z")), is(true));

        assertThat(chapterRepository.count(), is(0L));
    }


    /**
     * Given a valid CreateCourseInput
     * When the createCourse mutation is executed
     * Then the course is created and returned
     */
    @Test
    void testCreateCourseWithTerm(final HttpGraphQlTester tester) {
        final String currentUser = getCurrentUser();
        final String query = """
                mutation {
                    createCourse(
                        input: {
                            title: "New Course"
                            description: "This is a new course"
                            startDate: "2020-01-01T00:00:00.000Z"
                            endDate: "2021-01-01T00:00:00.000Z"
                            published: false
                            startYear: 2020
                            yearDivision: FIRST_SEMESTER
                        }
                    ) {
                        id
                        title
                        description
                        startDate
                        endDate
                        published
                        startYear
                        yearDivision
                        chapters {
                            elements {
                                id
                            }
                        }
                    }
                }""";

        final UUID id = tester.mutate()
                .header("CurrentUser", currentUser)
                .build()
                .document(query)
                .execute()
                .path("createCourse.title").entity(String.class).isEqualTo("New Course")
                .path("createCourse.description").entity(String.class).isEqualTo("This is a new course")
                .path("createCourse.startDate").entity(String.class).isEqualTo("2020-01-01T00:00:00.000Z")
                .path("createCourse.endDate").entity(String.class).isEqualTo("2021-01-01T00:00:00.000Z")
                .path("createCourse.startYear").entity(Integer.class).isEqualTo(2020)
                .path("createCourse.yearDivision").entity(String.class).isEqualTo("FIRST_SEMESTER")
                .path("createCourse.chapters.elements").entityList(String.class).hasSize(0)
                .path("createCourse.published").entity(Boolean.class).isEqualTo(false)
                .path("createCourse.id").entity(UUID.class).get();

        // check that the course was created in the database
        assertThat(courseRepository.count(), is(1L));
        final var course = courseRepository.findAll().get(0);
        assertThat(course.getId(), is(id));
        assertThat(course.getTitle(), is("New Course"));
        assertThat(course.getDescription(), is("This is a new course"));
        assertThat(course.isPublished(), is(false));
        assertThat(course.getStartDate().isEqual(OffsetDateTime.parse("2020-01-01T00:00:00.000Z")), is(true));
        assertThat(course.getEndDate().isEqual(OffsetDateTime.parse("2021-01-01T00:00:00.000Z")), is(true));
        assertThat(course.getStartYear(), is(2020));
        assertThat(course.getYearDivision(), is(YearDivision.FIRST_SEMESTER));

        assertThat(chapterRepository.count(), is(0L));
    }

    /**
     * Given a CreateCourseInput with a blank title
     * When the createCourse mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testErrorOnBlankTitle(final GraphQlTester tester) {
        final String query = """
                mutation {
                    createCourse(
                        input: {
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
     * Given a CreateCourseInput with a title that is too long
     * When the createCourse mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testTooLongTitle(final GraphQlTester tester) {
        final String query = String.format("""
                mutation {
                    createCourse(
                        input: {
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
     * Given a CreateCourseInput with a too long description
     * When the createCourse mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testTooLongDescription(final GraphQlTester tester) {
        final String query = String.format("""
                mutation {
                    createCourse(
                        input: {
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
     * Given a CreateCourseInput where the start date is after the end date
     * When the createCourse mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testStartDateAfterEndDate(final HttpGraphQlTester tester) {
        final String currentUser = getCurrentUser();

        final String query = """
                mutation {
                    createCourse(
                        input: {
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
                }""";

        tester.mutate()
                .header("CurrentUser", currentUser)
                .build()
                .document(query)
                .execute()
                .errors()
                .expect(responseError -> responseError.getMessage() != null
                                         && responseError.getMessage()
                                                 .toLowerCase().contains("start date must be before end date"));
    }

    private static String getCurrentUser() {
        final UUID userId = UUID.randomUUID();
        return """
                {
                    "id": "%s",
                    "userName": "MyUserName",
                    "firstName": "John",
                    "lastName": "Doe",
                    "courseMemberships": []
                }
                """.formatted(userId.toString());
    }
}
