package de.unistuttgart.iste.gits.course_service.api;


import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.common.testutil.MockTestPublisherConfiguration;
import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.generated.dto.*;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ContextConfiguration;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Tests that the courses query works correctly.
 */
@ContextConfiguration(classes = MockTestPublisherConfiguration.class)
@GraphQlApiTest
class QueryCoursesTest {

    @Autowired
    private CourseRepository courseRepository;

    private final ModelMapper modelMapper = new ModelMapper();

    /**
     * Given no courses exist
     * When the courses are queried
     * Then an empty list is returned and the pagination information is correct.
     */
    @Test
    void testGetCoursesEmpty(final GraphQlTester tester) {
        final String query = """
                query {
                    courses {
                        elements {
                            id
                        }
                        pagination {
                            totalElements
                            totalPages
                            page
                            size
                            hasNext
                        }
                    }
                }""";

        tester.document(query)
                .execute()
                .path("courses.elements").entityList(Course.class).hasSize(0)
                .path("courses.pagination.totalElements").entity(Integer.class).isEqualTo(0)
                .path("courses.pagination.totalPages").entity(Integer.class).isEqualTo(1)
                .path("courses.pagination.page").entity(Integer.class).isEqualTo(0)
                .path("courses.pagination.size").entity(Integer.class).isEqualTo(0)
                .path("courses.pagination.hasNext").entity(Boolean.class).isEqualTo(false);
    }

    /**
     * Given two courses exist
     * When the courses are queried
     * Then the courses are returned and the pagination information is correct.
     */
    @Test
    void testGetAllCourses(final GraphQlTester tester) {
        // create two courses in the database
        final var initialData = Stream.of(
                        dummyCourseBuilder().title("Course 1").build(),
                        dummyCourseBuilder().title("Course 2").build())
                .map(courseRepository::save)
                .toList();

        final String query = """
                query {
                    courses {
                        elements {
                            id
                            title
                            description
                            startDate
                            endDate
                            published
                        }
                        pagination {
                            totalElements
                            totalPages
                            page
                            size
                            hasNext
                        }
                    }
                }""";

        tester.document(query)
                .execute()
                .path("courses.elements").entityList(Course.class).hasSize(2)
                .contains(entitiesToDtos(initialData))
                .path("courses.pagination.totalElements").entity(Integer.class).isEqualTo(2)
                .path("courses.pagination.totalPages").entity(Integer.class).isEqualTo(1)
                .path("courses.pagination.page").entity(Integer.class).isEqualTo(0)
                .path("courses.pagination.size").entity(Integer.class).isEqualTo(2)
                .path("courses.pagination.hasNext").entity(Boolean.class).isEqualTo(false);
    }

    /**
     * Given two courses exist
     * When the courses are queried with pagination
     * Then the courses are returned and the pagination information is correct.
     */
    @Test
    void testGetAllCoursesWithPagination(final GraphQlTester tester) {
        final var data = Stream.of(
                        dummyCourseBuilder().title("Course 1").build(),
                        dummyCourseBuilder().title("Course 2").build(),
                        dummyCourseBuilder().title("Course 3").build(),
                        dummyCourseBuilder().title("Course 4").build())
                .map(courseRepository::save)
                .toList();

        final String query = """
                query($page: Int!) {
                    courses(pagination: {page: $page, size: 2}) {
                        elements {
                            id
                            title
                            description
                            startDate
                            endDate
                            published
                        }
                        pagination {
                            totalElements
                            totalPages
                            page
                            size
                            hasNext
                        }
                    }
                }""";

        tester.document(query)
                .variable("page", 0)
                .execute()
                .path("courses.elements").entityList(Course.class).hasSize(2)
                .contains(entitiesToDtos(data.subList(0, 2)))
                .path("courses.pagination.totalElements").entity(Integer.class).isEqualTo(4)
                .path("courses.pagination.totalPages").entity(Integer.class).isEqualTo(2)
                .path("courses.pagination.page").entity(Integer.class).isEqualTo(0)
                .path("courses.pagination.size").entity(Integer.class).isEqualTo(2)
                .path("courses.pagination.hasNext").entity(Boolean.class).isEqualTo(true);

        tester.document(query)
                .variable("page", 1)
                .execute()
                .path("courses.elements").entityList(Course.class).hasSize(2)
                .contains(entitiesToDtos(data.subList(2, 4)))
                .path("courses.pagination.totalElements").entity(Integer.class).isEqualTo(4)
                .path("courses.pagination.totalPages").entity(Integer.class).isEqualTo(2)
                .path("courses.pagination.page").entity(Integer.class).isEqualTo(1)
                .path("courses.pagination.size").entity(Integer.class).isEqualTo(2)
                .path("courses.pagination.hasNext").entity(Boolean.class).isEqualTo(false);

    }

    /**
     * Given a sort field and a sort direction
     * When querying all courses
     * Then the courses are sorted by the given field and direction
     * HINT: Test multiple sort fields in the future
     */
    @Test
    void testGetAllCoursesWithSort(final GraphQlTester tester) {
        final var data = Stream.of(
                        dummyCourseBuilder().description("A").build(),
                        dummyCourseBuilder().description("B").build(),
                        dummyCourseBuilder().description("C").build(),
                        dummyCourseBuilder().description("D").build())
                .map(courseRepository::save)
                .toList();

        final String query = """
                query($sortDirection: SortDirection!) {
                    courses(sortBy: "description", sortDirection: [$sortDirection]) {
                        elements {
                            id
                            title
                            description
                            startDate
                            endDate
                            published
                        }
                    }
                }""";

        tester.document(query)
                .variable("sortDirection", SortDirection.ASC)
                .execute()
                .path("courses.elements").entityList(Course.class).hasSize(4)
                .contains(entitiesToDtos(data));

        tester.document(query)
                .variable("sortDirection", SortDirection.DESC)
                .execute()
                .path("courses.elements").entityList(Course.class).hasSize(4)
                .contains(entitiesToDtos(List.of(data.get(3), data.get(2), data.get(1), data.get(0))));
    }

    /**
     * Given a filter
     * When querying all courses
     * Then the courses are filtered by the given filter
     * HINT: Maybe test more filter fields in the future
     */
    @Test
    void testGetCoursesWithFilter(final GraphQlTester tester) {
        final var data = Stream.of(
                        dummyCourseBuilder().title("Course 1").description("A").build(),
                        dummyCourseBuilder().title("Course 2").description("B").build(),
                        dummyCourseBuilder().title("Course 3").description("C").build(),
                        dummyCourseBuilder().title("Course 4").description("D").build())
                .map(courseRepository::save)
                .toList();

        final String query = """
                query($filter: CourseFilter!) {
                    courses(filter: $filter) {
                        elements {
                            id
                            title
                            description
                            startDate
                            endDate
                            published
                        }
                    }
                }""";

        // filter for title contains "Course"
        tester.document(query)
                .variable("filter", CourseFilter.builder()
                        .setTitle(StringFilter.builder()
                                .setContains("Course")
                                .build())
                        .build())
                .execute()
                .path("courses.elements").entityList(Course.class).hasSize(4)
                .contains(entitiesToDtos(data));

        // filter for title contains "Course 1"
        tester.document(query)
                .variable("filter",
                        CourseFilter.builder()
                                .setTitle(StringFilter.builder()
                                        .setContains("Course 1")
                                        .build())
                                .build())
                .execute()
                .path("courses.elements").entityList(Course.class).hasSize(1)
                .contains(entitiesToDtos(List.of(data.get(0))));

        // filter for description contains "A" or "B"
        tester.document(query)
                .variable("filter",
                        CourseFilter.builder()
                                .setDescription(StringFilter.builder()
                                        .setContains("A")
                                        .build())
                                .setOr(List.of(
                                        CourseFilter.builder()
                                                .setDescription(StringFilter.builder()
                                                        .setContains("B")
                                                        .build())
                                                .build()))
                                .build())
                .execute()
                .path("courses.elements").entityList(Course.class).hasSize(2)
                .contains(entitiesToDtos(data.subList(0, 2)));
    }

    /**
     * Given a course id
     * When querying a course by id
     * Then the course is returned
     */
    @Test
    void testGetByIds(final GraphQlTester tester) {
        // create two courses in the database
        final var initialData = Stream.of(
                        dummyCourseBuilder().title("Course 1").build(),
                        dummyCourseBuilder().title("Course 2").build())
                .map(courseRepository::save)
                .toList();

        final String query = """
                query {
                    coursesByIds(ids: ["%s"]) {
                        id
                        title
                        description
                        startDate
                        endDate
                        published
                    }
                }""".formatted(initialData.get(1).getId());

        tester.document(query)
                .execute()
                .path("coursesByIds").entityList(Course.class)
                .hasSize(1)
                .contains(entityToDto(initialData.get(1)));
    }

    /**
     * Given a course id of a not existing course
     * When querying a course by id
     * Then an error is returned
     */
    @Test
    void testGetByIdsWithNotExistingId(final GraphQlTester tester) {
        final String query = """
                query {
                    coursesByIds(ids: ["%s"]) {
                        id
                        title
                        description
                        startDate
                        endDate
                        published
                    }
                }""".formatted(UUID.randomUUID());

        tester.document(query)
                .execute()
                .errors()
                .expect(responseError -> requireNonNull(responseError.getMessage()).contains("Entities(s) with id(s) ")
                                         && responseError.getMessage().contains(" not found"));
    }

    private CourseEntity.CourseEntityBuilder dummyCourseBuilder() {
        return CourseEntity.builder()
                .title("Course 1")
                .description("This is course 1")
                .published(false)
                .startDate(OffsetDateTime.parse("2020-01-01T00:00:00.000Z"))
                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"));
    }

    private Course entityToDto(final CourseEntity entity) {
        return modelMapper.map(entity, Course.class);
    }

    private Course[] entitiesToDtos(final List<CourseEntity> entities) {
        return entities.stream().map(this::entityToDto).toArray(Course[]::new);
    }
}
