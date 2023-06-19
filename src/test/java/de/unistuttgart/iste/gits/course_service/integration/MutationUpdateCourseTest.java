package de.unistuttgart.iste.gits.course_service.integration;

import de.unistuttgart.iste.gits.common.testutil.GitsPostgresSqlContainer;
import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.course_service.persistence.dao.ChapterEntity;
import de.unistuttgart.iste.gits.course_service.persistence.dao.CourseEntity;
import de.unistuttgart.iste.gits.course_service.persistence.repository.ChapterRepository;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.generated.dto.Chapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.OffsetDateTime;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the `updateCourse` mutation.
 */
@GraphQlApiTest
class MutationUpdateCourseTest {

    @Container
    public static PostgreSQLContainer<GitsPostgresSqlContainer> postgreSQLContainer = GitsPostgresSqlContainer.getInstance();

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ChapterRepository chapterRepository;

    /**
     * Given a valid UpdateCourseInput
     * When the updateCourse mutation is executed
     * Then the course is updated and returned
     */
    @Test
    @Transactional
    void testUpdateCourseSuccessful(GraphQlTester tester) {
        // create a course with a chapter in the database
        var initialData = courseRepository.save(CourseEntity.builder().title("Course 1")
                .description("This is course 1")
                .startDate(OffsetDateTime.parse("2020-01-01T00:00:00.000Z"))
                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"))
                .published(true)
                .build());
        chapterRepository.save(ChapterEntity.builder()
                .courseId(initialData.getId())
                .title("Chapter 1")
                .description("This is chapter 1")
                .startDate(OffsetDateTime.parse("2020-01-01T00:00:00.000Z"))
                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"))
                .number(1)
                .build());

        String query = """
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
                }""".formatted(initialData.getId());

        tester.document(query)
                .execute()
                .path("updateCourse.id").entity(String.class).isEqualTo(initialData.getId().toString())
                .path("updateCourse.title").entity(String.class).isEqualTo("New Course")
                .path("updateCourse.description").entity(String.class).isEqualTo("This is a new course")
                .path("updateCourse.startDate").entity(String.class).isEqualTo("2000-01-01T00:00:00.000Z")
                .path("updateCourse.endDate").entity(String.class).isEqualTo("2001-01-01T00:00:00.000Z")
                .path("updateCourse.published").entity(Boolean.class).isEqualTo(false)
                .path("updateCourse.chapters.elements").entityList(Chapter.class).hasSize(1);

        // check that the course was updated in the database
        var updatedCourse = courseRepository.findById(initialData.getId()).orElseThrow();
        assertThat(updatedCourse.getTitle(), is("New Course"));
        assertThat(updatedCourse.getDescription(), is("This is a new course"));
        assertThat(updatedCourse.isPublished(), is(false));
        assertThat(updatedCourse.getStartDate().isEqual(OffsetDateTime.parse("2000-01-01T00:00:00.000Z")), is(true));
        assertThat(updatedCourse.getEndDate().isEqual(OffsetDateTime.parse("2001-01-01T00:00:00.000Z")), is(true));

        // check that the chapter was not deleted
        var chapters = chapterRepository.findAll();
        assertThat(chapters.size(), is(1));
        // assertThat(chapters.get(0).getCourse().getId(), is(initialData.getId()));
    }

    /**
     * Given a UpdateCourseInput with a non-existing id
     * When the createCourse mutation is executed
     * Then an error is returned
     */
    @Test
    void testUpdateCourseNotExisting(GraphQlTester tester) {
        UUID id = UUID.randomUUID();
        String query = String.format("""
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
                }""", id);

        tester.document(query)
                .execute()
                .errors()
                .expect(responseError -> requireNonNull(responseError.getMessage())
                        .contains("Course with id " + id + " not found"));
    }

    /**
     * Given a UpdateCourseInput with a blank title
     * When the createCourse mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testErrorOnBlankTitle(GraphQlTester tester) {
        String query = """
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
    void testTooLongTitle(GraphQlTester tester) {
        String query = String.format("""
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
    void testTooLongDescription(GraphQlTester tester) {
        String query = String.format("""
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
    void testStartDateAfterEndDate(GraphQlTester tester) {
        String query = """
                mutation {
                    updateCourse(
                        input: {
                            id: "00000000-0000-0000-0000-000000000000"
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

        tester.document(query)
                .execute()
                .errors()
                .expect(responseError -> responseError.getMessage() != null
                                         && responseError.getMessage()
                                                 .toLowerCase().contains("start date must be before end date"));
    }
}
