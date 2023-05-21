package de.unistuttgart.iste.gits.courseservice;

import de.unistuttgart.iste.gits.courseservice.dto.CourseDto;
import de.unistuttgart.iste.gits.courseservice.dto.CreateCourseInputDto;
import de.unistuttgart.iste.gits.util.GraphQlIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tests for the GraphQL API.
 * <p>
 * HINT: This is just an example. This class should be extended with more tests.
 * <p>
 * HINT: requires an empty database. We need to use a test database for this in the future.
 */
@ExtendWith(GraphQlIntegrationTest.class)
public class CourseIntegrationTest {

    private List<UUID> uuids = new ArrayList<>();

    @BeforeEach
    public void setup() {
        uuids.clear();
    }

    @AfterEach
    public void cleanup(GraphQlTester tester) {
        for (UUID uuid : uuids) {
            deleteCourse(tester, uuid);
        }
    }

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

        UUID id = tester.document(query)
                .execute()
                .path("createCourse.title").entity(String.class).isEqualTo("New Course")
                .path("createCourse.description").entity(String.class).isEqualTo("This is a new course")
                .path("createCourse.startDate").entity(String.class).isEqualTo("2020-01-01T00:00:00.000Z")
                .path("createCourse.endDate").entity(String.class).isEqualTo("2021-01-01T00:00:00.000Z")
                .path("createCourse.chapters").entityList(String.class).hasSize(0)
                .path("createCourse.published").entity(Boolean.class).isEqualTo(false)
                .path("createCourse.id").entity(UUID.class).get();

        uuids.add(id);
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
        var inputDtos = List.of(CreateCourseInputDto.builder()
                        .setTitle("Course 1")
                        .setDescription("Description 1")
                        .setStartDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"))
                        .setEndDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"))
                        .setPublished(false)
                        .build(),
                CreateCourseInputDto.builder()
                        .setTitle("Course 2")
                        .setDescription("Description 2")
                        .setStartDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"))
                        .setEndDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"))
                        .setPublished(true)
                        .build());
        uuids = List.of(
                createCourse(tester, inputDtos.get(0)),
                createCourse(tester, inputDtos.get(1))
        );


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
                .path("courses").entityList(CourseDto.class).hasSize(2).contains(
                        courseDtoFromInput(inputDtos.get(0), uuids.get(0)),
                        courseDtoFromInput(inputDtos.get(1), uuids.get(1))
                );
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

    private UUID createCourse(GraphQlTester tester, CreateCourseInputDto input) {
        return tester.documentName("create-course")
                .variable("title", input.getTitle())
                .variable("description", input.getDescription())
                .variable("startDate", input.getStartDate().toString())
                .variable("endDate", input.getEndDate().toString())
                .variable("published", input.getPublished())
                .execute()
                .path("createCourse.id").entity(UUID.class).get();
    }

    private CourseDto courseDtoFromInput(CreateCourseInputDto input, UUID id) {
        return new CourseDto(
                id,
                input.getTitle(),
                input.getDescription(),
                input.getStartDate(),
                input.getEndDate(),
                input.getPublished(),
                List.of()
        );
    }

    private void deleteCourse(GraphQlTester tester, UUID id) {
        tester.documentName("delete-course")
                .variable("uuid", id)
                .execute()
                .path("deleteCourse").entity(UUID.class).isEqualTo(id);
    }
}
