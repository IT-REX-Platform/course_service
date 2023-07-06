package de.unistuttgart.iste.gits.course_service.integration;

import de.unistuttgart.iste.gits.common.testutil.GitsPostgresSqlContainer;
import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.course_service.persistence.dao.CourseEntity;
import de.unistuttgart.iste.gits.course_service.persistence.repository.ChapterRepository;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.course_service.test_config.MockTopicPublisherConfiguration;
import de.unistuttgart.iste.gits.generated.dto.Chapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.OffsetDateTime;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ContextConfiguration(classes = MockTopicPublisherConfiguration.class)
@GraphQlApiTest
class MutationCreateChapterTest {

    @Container
    public static PostgreSQLContainer<GitsPostgresSqlContainer> postgreSQLContainer = GitsPostgresSqlContainer.getInstance();

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    /**
     * Given a valid CreateChapterInput
     * When the createChapter mutation is executed
     * Then the chapter is created and returned
     */
    @Test
    void testCreateChapter(GraphQlTester tester) {
        var course = courseRepository.save(CourseEntity.builder()
                .title("New Course")
                .description("This is a new course")
                .startDate(OffsetDateTime.parse("2020-01-01T00:00:00.000Z"))
                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"))
                .published(false)
                .build());

        String query = """
                mutation {
                    createChapter(
                        input: {
                            courseId: "%s"
                            title: "New Chapter"
                            description: "This is a new chapter"
                            startDate: "2020-01-01T00:00:00.000Z"
                            endDate: "2021-01-01T00:00:00.000Z"
                            number: 1
                        }
                    ) {
                        id
                        title
                        description
                        startDate
                        endDate
                        number
                    }
                }""".formatted(course.getId());

        tester.document(query)
                .execute()
                .path("createChapter")
                .entity(Chapter.class)
                .satisfies(chapter -> {
                    assertThat(chapter.getTitle(), is("New Chapter"));
                    assertThat(chapter.getDescription(), is("This is a new chapter"));
                    assertThat(chapter.getStartDate(), is(OffsetDateTime.parse("2020-01-01T00:00:00.000Z")));
                    assertThat(chapter.getEndDate(), is(OffsetDateTime.parse("2021-01-01T00:00:00.000Z")));
                    assertThat(chapter.getNumber(), is(1));
                });

        assertThat(chapterRepository.count(), is(1L));
        var chapter = chapterRepository.findAll().get(0);
        assertThat(chapter.getTitle(), is("New Chapter"));
        assertThat(chapter.getDescription(), is("This is a new chapter"));
        assertThat(chapter.getStartDate().isEqual(OffsetDateTime.parse("2020-01-01T00:00:00.000Z")), is(true));
        assertThat(chapter.getEndDate().isEqual(OffsetDateTime.parse("2021-01-01T00:00:00.000Z")), is(true));
        assertThat(chapter.getNumber(), is(1));
    }

    /**
     * Given a CreateChapterInput with a courseId that does not exist
     * When the createChapter mutation is executed
     * Then an error is returned
     */
    @Test
    void testCreateChapterCourseNotExisting(GraphQlTester tester) {
        String query = """
                mutation {
                    createChapter(
                        input: {
                            courseId: "00000000-0000-0000-0000-000000000000"
                            title: "New Chapter"
                            description: "This is a new chapter"
                            startDate: "2020-01-01T00:00:00.000Z"
                            endDate: "2021-01-01T00:00:00.000Z"
                            number: 1
                        }
                    ) {
                        id
                        title
                        description
                        startDate
                        endDate
                        number
                    }
                }""";

        tester.document(query)
                .execute()
                .errors()
                .expect(responseError -> Objects.requireNonNull(responseError.getMessage())
                        .contains("Course with id 00000000-0000-0000-0000-000000000000 not found"));

        assertThat(chapterRepository.count(), is(0L));
    }

    /**
     * Given a CreateChapterInput with a blank title
     * When the createChapter mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testErrorOnBlankTitle(GraphQlTester tester) {
        String query = """
                mutation {
                    createChapter(
                        input: {
                            courseId: "00000000-0000-0000-0000-000000000000"
                            title: " "
                            description: "This is a new chapter"
                            startDate: "2020-01-01T00:00:00.000Z"
                            endDate: "2021-01-01T00:00:00.000Z"
                            number: 1
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
     * Given a CreateChapterInput with a title that is too long
     * When the createChapter mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testTooLongTitle(GraphQlTester tester) {
        String query = String.format("""
                mutation {
                    createChapter(
                        input: {
                            courseId: "00000000-0000-0000-0000-000000000000"
                            title: "%s"
                            description: "This is a new chapter"
                            startDate: "2020-01-01T00:00:00.000Z"
                            endDate: "2021-01-01T00:00:00.000Z"
                            number: 1
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
     * Given a CreateChapterInput with a too long description
     * When the createChapter mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testTooLongDescription(GraphQlTester tester) {
        String query = String.format("""
                mutation {
                    createChapter(
                        input: {
                            courseId: "00000000-0000-0000-0000-000000000000"
                            title: "New Chapter"
                            description: "%s"
                            startDate: "2020-01-01T00:00:00.000Z"
                            endDate: "2021-01-01T00:00:00.000Z"
                            number: 1
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
     * Given a CreateChapterInput where the start date is after the end date
     * When the createChapter mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testStartDateAfterEndDate(GraphQlTester tester) {
        String query = """
                mutation {
                    createChapter(
                        input: {
                            courseId: "00000000-0000-0000-0000-000000000000"
                            title: "New Chapter"
                            description: "This is a new chapter"
                            startDate: "2021-01-01T00:00:00.000Z"
                            endDate: "2020-01-01T00:00:00.000Z"
                            number: 1
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

    /**
     * Given a CreateChapterInput where the suggested start date is after the end date
     * When the createChapter mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testSuggestedStartDateAfterEndDate(GraphQlTester tester) {
        String query = """
                mutation {
                    createChapter(
                        input: {
                            courseId: "00000000-0000-0000-0000-000000000000"
                            title: "New Chapter"
                            description: "This is a new chapter"
                            startDate: "2021-01-01T00:00:00.000Z"
                            endDate: "2021-01-02T00:00:00.000Z"
                            suggestedStartDate: "2021-01-03T00:00:00.000Z"
                            number: 1
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
                        .toLowerCase().contains("suggested start date must be before end date"));
    }

    /**
     * Given a CreateChapterInput where the suggested start date is after the suggested end date
     * When the createChapter mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testSuggestedStartDateAfterSuggestedEndDate(GraphQlTester tester) {
        String query = """
                mutation {
                    createChapter(
                        input: {
                            courseId: "00000000-0000-0000-0000-000000000000"
                            title: "New Chapter"
                            description: "This is a new chapter"
                            startDate: "2020-01-01T00:00:00.000Z"
                            endDate: "2021-02-02T00:00:00.000Z"
                            suggestedStartDate: "2021-01-03T00:00:00.000Z"
                            suggestedEndDate: "2020-12-01T00:00:00.000Z"
                            number: 1
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
                        .toLowerCase().contains("suggested start date must be before suggested end date"));
    }

    /**
     * Given a CreateChapterInput where the suggested start date is before the start date
     * When the createChapter mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testSuggestedStartDateBeforeStartDate(GraphQlTester tester) {
        String query = """
                mutation {
                    createChapter(
                        input: {
                            courseId: "00000000-0000-0000-0000-000000000000"
                            title: "New Chapter"
                            description: "This is a new chapter"
                            startDate: "2021-01-01T00:00:00.000Z"
                            endDate: "2021-02-02T00:00:00.000Z"
                            suggestedStartDate: "2020-01-03T00:00:00.000Z"
                            number: 1
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
                        .toLowerCase().contains("suggested start date must be after start date"));
    }

    /**
     * Given a CreateChapterInput where the suggested end date is before the start date
     * When the createChapter mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testSuggestedEndDateBeforeStartDate(GraphQlTester tester) {
        String query = """
                mutation {
                    createChapter(
                        input: {
                            courseId: "00000000-0000-0000-0000-000000000000"
                            title: "New Chapter"
                            description: "This is a new chapter"
                            startDate: "2021-01-01T00:00:00.000Z"
                            endDate: "2021-02-02T00:00:00.000Z"
                            suggestedEndDate: "2020-01-03T00:00:00.000Z"
                            number: 1
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
                        .toLowerCase().contains("suggested end date must be after start date"));
    }

    /**
     * Given a CreateChapterInput where the suggested end date is after the end date
     * When the createChapter mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testSuggestedEndDateAfterEndDate(GraphQlTester tester) {
        String query = """
                mutation {
                    createChapter(
                        input: {
                            courseId: "00000000-0000-0000-0000-000000000000"
                            title: "New Chapter"
                            description: "This is a new chapter"
                            startDate: "2021-01-01T00:00:00.000Z"
                            endDate: "2021-01-02T00:00:00.000Z"
                            suggestedEndDate: "2021-01-03T00:00:00.000Z"
                            number: 1
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
                        .toLowerCase().contains("suggested end date must be before end date"));
    }

}
