package de.unistuttgart.iste.gits.courseservice.integration;

import de.unistuttgart.iste.gits.courseservice.dto.ChapterDto;
import de.unistuttgart.iste.gits.courseservice.dto.CourseDto;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.CourseEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.util.GraphQlApiTest;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;

/**
 * Tests for the GraphQL API.
 */
@GraphQlApiTest
public class CourseIntegrationTest {

    @Autowired
    private CourseRepository courseRepository;

    private final ModelMapper modelMapper = new ModelMapper();

    /**
     * Tests that a course can be created and the correct values are returned.
     */
    @Test
    public void testCreateCourse(GraphQlTester tester) {
        String query = """
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
                            id
                        }
                    }
                }""";

        tester.document(query)
                .execute()
                .path("createCourse.title").entity(String.class).isEqualTo("New Course")
                .path("createCourse.description").entity(String.class).isEqualTo("This is a new course")
                .path("createCourse.startDate").entity(String.class).isEqualTo("2020-01-01T00:00:00.000Z")
                .path("createCourse.endDate").entity(String.class).isEqualTo("2021-01-01T00:00:00.000Z")
                .path("createCourse.chapters").entityList(String.class).hasSize(0)
                .path("createCourse.published").entity(Boolean.class).isEqualTo(false);
    }

    /**
     * Tests that an empty list is returned if no courses exist.
     */
    @Test
    public void testGetCoursesEmpty(GraphQlTester tester) {
        String query = """
                query {
                    courses {
                        id
                    }
                }""";

        tester.document(query)
                .execute()
                .path("courses").entityList(CourseDto.class).hasSize(0);
    }

    /**
     * Test that the courses can be retrieved correctly.
     */
    @Test
    public void testGetCourses(GraphQlTester tester) {
        // create two courses in the database
        var initialData = Stream.of(
                        CourseEntity.builder().title("Course 1")
                                .description("This is course 1")
                                .startDate(OffsetDateTime.parse("2020-01-01T00:00:00.000Z"))
                                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"))
                                .chapters(List.of())
                                .published(false).build(),
                        CourseEntity.builder().title("Course 2")
                                .description("This is course 2")
                                .startDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"))
                                .endDate(OffsetDateTime.parse("2022-02-01T00:00:00.000Z"))
                                .chapters(List.of())
                                .published(true).build())
                .map(courseRepository::save)
                .toList();

        String query = """
                query {
                    courses {
                        id
                        title
                        description
                        startDate
                        endDate
                        published
                        chapters {
                            id
                        }
                    }
                }""";

        tester.document(query)
                .execute()
                .path("courses").entityList(CourseDto.class)
                .hasSize(2)
                .contains(
                        entityToDto(initialData.get(0)),
                        entityToDto(initialData.get(1))
                );
    }

    /**
     * Tests that courses can be retrieved by its id.
     */
    @Test
    public void testGetByIds(GraphQlTester tester) {
        // create two courses in the database
        var initialData = Stream.of(
                        CourseEntity.builder().title("Course 1")
                                .description("This is course 1")
                                .startDate(OffsetDateTime.parse("2020-01-01T00:00:00.000Z"))
                                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"))
                                .chapters(List.of())
                                .published(false).build(),
                        CourseEntity.builder().title("Course 2")
                                .description("This is course 2")
                                .startDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"))
                                .endDate(OffsetDateTime.parse("2022-02-01T00:00:00.000Z"))
                                .chapters(List.of())
                                .published(true).build())
                .map(courseRepository::save)
                .toList();

        String query = """
                query {
                    coursesById(ids: ["%s"]) {
                        id
                        title
                        description
                        startDate
                        endDate
                        published
                        chapters {
                            id
                        }
                    }
                }""".formatted(initialData.get(1).getId());

        tester.document(query)
                .execute()
                .path("coursesById").entityList(CourseDto.class)
                .hasSize(1)
                .contains(entityToDto(initialData.get(1)));
    }

    /**
     * Test that a course can be updated.
     */
    @Test
    public void testUpdateCourseSuccessful(GraphQlTester tester) {
        // create a course in the database
        var initialData = courseRepository.save(CourseEntity.builder().title("Course 1")
                .description("This is course 1")
                .startDate(OffsetDateTime.parse("2020-01-01T00:00:00.000Z"))
                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"))
                .chapters(List.of())
                .published(true).build());

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
                            id
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
                .path("updateCourse.chapters").entityList(ChapterDto.class).hasSize(0);
    }

    /**
     * Tests that after deleting a course, it is no longer returned.
     */
    @Test
    public void testDeletion(GraphQlTester tester) {
        // create two courses in the database
        var initialData = Stream.of(
                        CourseEntity.builder().title("Course 1")
                                .description("This is course 1")
                                .startDate(OffsetDateTime.parse("2020-01-01T00:00:00.000Z"))
                                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"))
                                .chapters(List.of())
                                .published(false).build(),
                        CourseEntity.builder().title("Course 2")
                                .description("This is course 2")
                                .startDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"))
                                .endDate(OffsetDateTime.parse("2022-02-01T00:00:00.000Z"))
                                .chapters(List.of())
                                .published(true).build())
                .map(courseRepository::save)
                .toList();

        String query = """
                mutation {
                    deleteCourse(id: "%s")
                }""".formatted(initialData.get(0).getId());

        tester.document(query)
                .execute()
                .path("deleteCourse").entity(UUID.class).isEqualTo(initialData.get(0).getId());

        String getCoursesQuery = """
                query {
                    courses {
                        id
                    }
                }""";

        tester.document(getCoursesQuery)
                .execute()
                .path("courses").entityList(Object.class).hasSize(1)
                .path("courses[0].id").entity(UUID.class).isEqualTo(initialData.get(1).getId());
    }

    /**
     * Tests that errors are returned on update and delete if the course does not exist.
     */
    @Test
    public void testErrorOnNonExistentCourse(GraphQlTester tester) {
        String updateQuery = """
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
                    ) { id }
                }""".formatted(UUID.randomUUID());

        tester.document(updateQuery)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors, hasSize(1));
                    assertThat(responseErrors.get(0).getMessage(), containsString("Course with id"));
                    assertThat(responseErrors.get(0).getMessage(), containsString("not found"));
                });

        String deleteQuery = """
                mutation {
                    deleteCourse(id: "%s")
                }""".formatted(UUID.randomUUID());

        tester.document(deleteQuery)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors, hasSize(1));
                    assertThat(responseErrors.get(0).getMessage(), containsString("Course with id"));
                    assertThat(responseErrors.get(0).getMessage(), containsString("not found"));
                });
    }

    /**
     * Tests that an error is returned if the title is blank.
     */
    @Test
    public void testErrorOnBlankTitle(GraphQlTester tester) {
        String query = """
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
     * Tests that an error is returned if the title is too long.
     */
    @Test
    public void testTooLongTitle(GraphQlTester tester) {
        String query = String.format("""
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
     * Tests that an error is returned if the start date is after the end date.
     */
    @Test
    public void testStartDateAfterEndDate(GraphQlTester tester) {
        String query = """
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

        tester.document(query)
                .execute()
                .errors()
                .expect(responseError -> responseError.getMessage() != null
                                         && responseError.getMessage()
                                                 .toLowerCase().contains("start date must be before end date"));
    }

    private CourseDto entityToDto(CourseEntity courseEntity) {
        return modelMapper.map(courseEntity, CourseDto.class);
    }

}
