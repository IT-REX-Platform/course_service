package de.unistuttgart.iste.gits.courseservice.integration;

import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.ChapterEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.CourseEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.repository.ChapterRepository;
import de.unistuttgart.iste.gits.courseservice.persistence.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for the `deleteChapter` mutation.
 */
@GraphQlApiTest
class MutationDeleteChapterTest {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ChapterRepository chapterRepository;

    /**
     * Given a valid chapter id
     * When the deleteChapter mutation is executed
     * Then the chapter is deleted and the uuid is returned
     */
    @Test
    void testDeletion(GraphQlTester tester) {
        // create a course in the database
        var course = courseRepository.save(dummyCourseBuilder().build());
        // create two chapters in the database
        var chapters = Stream.of(
                        dummyChapterBuilder().courseId(course.getId()).title("Chapter 1").build(),
                        dummyChapterBuilder().courseId(course.getId()).title("Chapter 2").build())
                .map(chapterRepository::save)
                .toList();

        String query = """
                mutation {
                    deleteChapter(id: "%s")
                }""".formatted(chapters.get(0).getId());

        tester.document(query)
                .execute()
                .path("deleteChapter").entity(UUID.class).isEqualTo(chapters.get(0).getId());

        var entities = chapterRepository.findAll();
        assertThat(entities, hasSize(1));
        assertThat(entities.get(0).getId(), equalTo(chapters.get(1).getId()));
    }

    /**
     * Given an invalid chapter id
     * When the deleteChapter mutation is executed
     * Then an error is returned
     */
    @Test
    void testDeletionInvalidId(GraphQlTester tester) {
        String query = """
                mutation {
                    deleteChapter(id: "%s")
                }""".formatted(UUID.randomUUID());

        tester.document(query)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors, hasSize(1));
                    assertThat(responseErrors.get(0).getMessage(), containsString("Chapter with id"));
                    assertThat(responseErrors.get(0).getMessage(), containsString("not found"));
                });
    }

    private CourseEntity.CourseEntityBuilder dummyCourseBuilder() {
        return CourseEntity.builder()
                .title("Chapter 1")
                .description("This is chapter 1")
                .published(false)
                .startDate(OffsetDateTime.parse("2020-01-01T00:00:00.000Z"))
                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"));
    }

    private ChapterEntity.ChapterEntityBuilder dummyChapterBuilder() {
        return ChapterEntity.builder()
                .title("Chapter 1")
                .description("This is chapter 1")
                .number(1)
                .startDate(OffsetDateTime.parse("2020-01-01T00:00:00.000Z"))
                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"));
    }
}
