package de.unistuttgart.iste.gits.courseservice.integration;

import de.unistuttgart.iste.gits.courseservice.persistence.dao.ChapterEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.CourseEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.repository.ChapterRepository;
import de.unistuttgart.iste.gits.courseservice.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.util.GraphQlApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for the `deleteCourse` mutation.
 */
@GraphQlApiTest
public class MutationDeleteCourseTest {

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
    public void testDeletion(GraphQlTester tester) {
        // create two courses in the database
        var initialData = Stream.of(
                        dummyCourseBuilder().title("Course 1").build(),
                        dummyCourseBuilder().title("Course 2").build())
                .map(courseRepository::save)
                .toList();
        // create a chapter in the database to check that it is deleted
        chapterRepository.save(dummyChapterBuilder().courseId(initialData.get(0).getId()).build());

        String query = """
                mutation {
                    deleteCourse(id: "%s")
                }""".formatted(initialData.get(0).getId());

        tester.document(query)
                .execute()
                .path("deleteCourse").entity(UUID.class).isEqualTo(initialData.get(0).getId());

        var entities = courseRepository.findAll();
        assertThat(entities, hasSize(1));
        // check that the correct course was deleted and the other one is still there
        assertThat(entities.get(0).getId(), equalTo(initialData.get(1).getId()));
        // check that the chapter was deleted
        assertThat(chapterRepository.findAll(), hasSize(0));
    }

    /**
     * Given an invalid course id
     * When the deleteCourse mutation is executed
     * Then an error is returned
     */
    @Test
    public void testDeletionInvalidId(GraphQlTester tester) {
        String query = """
                mutation {
                    deleteCourse(id: "%s")
                }""".formatted(UUID.randomUUID());

        tester.document(query)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors, hasSize(1));
                    assertThat(responseErrors.get(0).getMessage(), containsString("Course with id"));
                    assertThat(responseErrors.get(0).getMessage(), containsString("not found"));
                });
    }

    private CourseEntity.CourseEntityBuilder dummyCourseBuilder() {
        return CourseEntity.builder()
                .title("Course 1")
                .description("This is course 1")
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
