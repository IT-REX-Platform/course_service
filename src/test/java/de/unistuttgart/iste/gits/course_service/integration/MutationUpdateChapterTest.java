package de.unistuttgart.iste.gits.course_service.integration;

import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.course_service.persistence.dao.ChapterEntity;
import de.unistuttgart.iste.gits.course_service.persistence.dao.CourseEntity;
import de.unistuttgart.iste.gits.course_service.persistence.repository.ChapterRepository;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.generated.dto.ChapterDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.time.OffsetDateTime;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@GraphQlApiTest
class MutationUpdateChapterTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    /**
     * Given a valid UpdateChapterInput
     * When the updateChapter mutation is executed
     * Then the chapter is updated and returned
     */
    @Test
    void testUpdateChapter(GraphQlTester tester) {
        var course = courseRepository.save(CourseEntity.builder()
                .title("New Course")
                .description("This is a new course")
                .startDate(OffsetDateTime.parse("2020-01-01T00:00:00.000Z"))
                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"))
                .published(false)
                .build());
        var chapter = chapterRepository.save(ChapterEntity.builder()
                .courseId(course.getId())
                .title("Old Chapter")
                .description("This is an old chapter")
                .startDate(OffsetDateTime.parse("2020-01-01T00:00:00.000Z"))
                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"))
                .number(1)
                .build());

        String query = """
                mutation {
                    updateChapter(
                        input: {
                            id: "%s"
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
                }""".formatted(chapter.getId());

        tester.document(query)
                .execute()
                .path("updateChapter")
                .entity(ChapterDto.class)
                .satisfies(chapterDto -> {
                    assertThat(chapterDto.getTitle(), is("New Chapter"));
                    assertThat(chapterDto.getDescription(), is("This is a new chapter"));
                    assertThat(chapterDto.getStartDate(), is(OffsetDateTime.parse("2020-01-01T00:00:00.000Z")));
                    assertThat(chapterDto.getEndDate(), is(OffsetDateTime.parse("2021-01-01T00:00:00.000Z")));
                    assertThat(chapterDto.getNumber(), is(1));
                });

        assertThat(chapterRepository.count(), is(1L));
        var updatedChapter = chapterRepository.findAll().get(0);
        assertThat(updatedChapter.getTitle(), is("New Chapter"));
        assertThat(updatedChapter.getDescription(), is("This is a new chapter"));
        assertThat(updatedChapter.getStartDate().isEqual(OffsetDateTime.parse("2020-01-01T00:00:00.000Z")), is(true));
        assertThat(updatedChapter.getEndDate().isEqual(OffsetDateTime.parse("2021-01-01T00:00:00.000Z")), is(true));
        assertThat(updatedChapter.getNumber(), is(1));
    }

    /**
     * Given a UpdateChapterInput with an id that does not exist
     * When the updateChapter mutation is executed
     * Then an error is returned
     */
    @Test
    void testUpdateChapterNotExisting(GraphQlTester tester) {
        String query = """
                mutation {
                    updateChapter(
                        input: {
                            id: "00000000-0000-0000-0000-000000000000"
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
                        .contains("Chapter with id 00000000-0000-0000-0000-000000000000 not found"));

        assertThat(chapterRepository.count(), is(0L));
    }

    /**
     * Given a UpdateChapterInput with a blank title
     * When the updateChapter mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testErrorOnBlankTitle(GraphQlTester tester) {
        String query = """
                mutation {
                    updateChapter(
                        input: {
                            id: "00000000-0000-0000-0000-000000000000"
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
     * Given a UpdateChapterInput with a title that is too long
     * When the updateChapter mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testTooLongTitle(GraphQlTester tester) {
        String query = String.format("""
                mutation {
                    updateChapter(
                        input: {
                            id: "00000000-0000-0000-0000-000000000000"
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
     * Given a UpdateChapterInput with a too long description
     * When the updateChapter mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testTooLongDescription(GraphQlTester tester) {
        String query = String.format("""
                mutation {
                    updateChapter(
                        input: {
                            id: "00000000-0000-0000-0000-000000000000"
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
     * Given a UpdateChapterInput where the start date is after the end date
     * When the updateChapter mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testStartDateAfterEndDate(GraphQlTester tester) {
        String query = """
                mutation {
                    updateChapter(
                        input: {
                            id: "00000000-0000-0000-0000-000000000000"
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
}
