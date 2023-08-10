package de.unistuttgart.iste.gits.course_service.integration;

import de.unistuttgart.iste.gits.common.testutil.GitsPostgresSqlContainer;
import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.course_service.persistence.dao.ChapterEntity;
import de.unistuttgart.iste.gits.course_service.persistence.dao.CourseEntity;
import de.unistuttgart.iste.gits.course_service.persistence.repository.ChapterRepository;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.course_service.test_config.MockTopicPublisherConfiguration;
import de.unistuttgart.iste.gits.generated.dto.Chapter;
import de.unistuttgart.iste.gits.generated.dto.ChapterFilter;
import de.unistuttgart.iste.gits.generated.dto.SortDirection;
import de.unistuttgart.iste.gits.generated.dto.StringFilter;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Test class for the chapters query.
 */
@ContextConfiguration(classes = MockTopicPublisherConfiguration.class)
@GraphQlApiTest
class QueryChaptersTest {

    @Container
    public static PostgreSQLContainer<GitsPostgresSqlContainer> postgreSQLContainer = GitsPostgresSqlContainer.getInstance();

    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private CourseRepository courseRepository;

    private final ModelMapper modelMapper = new ModelMapper();

    /**
     * Given no chapters exist
     * When the chapters are queried
     * Then an empty list is returned and the pagination information is correct.
     */
    @Test
    void testGetChaptersEmpty(GraphQlTester tester) {
        CourseEntity course = courseRepository.save(dummyCourseBuilder().build());

        String query = String.format("""
                query {
                    chapters(courseId: "%s") {
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
                }""", course.getId());

        tester.document(query)
                .execute()
                .path("chapters.elements").entityList(Chapter.class).hasSize(0)
                .path("chapters.pagination.totalElements").entity(Integer.class).isEqualTo(0)
                .path("chapters.pagination.totalPages").entity(Integer.class).isEqualTo(1)
                .path("chapters.pagination.page").entity(Integer.class).isEqualTo(0)
                .path("chapters.pagination.size").entity(Integer.class).isEqualTo(0)
                .path("chapters.pagination.hasNext").entity(Boolean.class).isEqualTo(false);
    }

    @Test
    void testGetChaptersByIds(GraphQlTester tester) {
        UUID courseId = UUID.randomUUID();
        List<ChapterEntity> expectedChapters = Stream.of(
                        dummyChapterBuilder().title("Chapter 1").courseId(courseId).build(),
                        dummyChapterBuilder().title("Chapter 2").courseId(courseId).build())
                .map(chapterRepository::save)
                .toList();

        String query = """
                query($chapterIds: [UUID!]!) {
                    chaptersByIds(ids: $chapterIds) {
                        id
                        title
                        description
                        startDate
                        endDate
                        number
                    }
                }
                """;

        tester.document(query)
                .variable("chapterIds", List.of(expectedChapters.get(0).getId(), expectedChapters.get(1).getId()))
                .execute()
                .path("chaptersByIds").entityList(Chapter.class).hasSize(2)
                .contains(entitiesToDtos(expectedChapters));
    }

    /**
     * Given two chapters exist
     * When the chapters are queried
     * Then the chapters are returned and the pagination information is correct.
     */
    @Test
    void testGetAllChapters(GraphQlTester tester) {
        CourseEntity course = courseRepository.save(dummyCourseBuilder().build());
        CourseEntity anotherCourse = courseRepository.save(dummyCourseBuilder().build());
        // create two chapters in the database
        var initialData = Stream.of(
                        dummyChapterBuilder().title("Chapter 1").courseId(course.getId()).build(),
                        dummyChapterBuilder().title("Chapter 2").courseId(course.getId()).build())
                .map(chapterRepository::save)
                .toList();
        // create a chapter in another course
        chapterRepository.save(dummyChapterBuilder().title("Chapter 3").courseId(anotherCourse.getId()).build());

        String query = String.format("""
                query {
                    chapters(courseId: "%s") {
                        elements {
                            id
                            title
                            description
                            startDate
                            endDate
                            number
                        }
                        pagination {
                            totalElements
                            totalPages
                            page
                            size
                            hasNext
                        }
                    }
                }""", course.getId());

        tester.document(query)
                .execute()
                .path("chapters.elements").entityList(Chapter.class).hasSize(2)
                .contains(entitiesToDtos(initialData))
                .path("chapters.pagination.totalElements").entity(Integer.class).isEqualTo(2)
                .path("chapters.pagination.totalPages").entity(Integer.class).isEqualTo(1)
                .path("chapters.pagination.page").entity(Integer.class).isEqualTo(0)
                .path("chapters.pagination.size").entity(Integer.class).isEqualTo(2)
                .path("chapters.pagination.hasNext").entity(Boolean.class).isEqualTo(false);
    }

    /**
     * Given two chapters exist
     * When the chapters are queried with pagination
     * Then the chapters are returned and the pagination information is correct.
     */
    @Test
    void testGetAllChaptersWithPagination(GraphQlTester tester) {
        CourseEntity course = courseRepository.save(dummyCourseBuilder().build());
        var data = Stream.of(
                        dummyChapterBuilder().title("Chapter 1").courseId(course.getId()).build(),
                        dummyChapterBuilder().title("Chapter 2").courseId(course.getId()).build(),
                        dummyChapterBuilder().title("Chapter 3").courseId(course.getId()).build(),
                        dummyChapterBuilder().title("Chapter 4").courseId(course.getId()).build())
                .map(chapterRepository::save)
                .toList();

        String query = """
                query($courseId: UUID!, $page: Int!) {
                    chapters(courseId: $courseId, pagination: {page: $page, size: 2}) {
                        elements {
                            id
                            title
                            description
                            startDate
                            endDate
                            number
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
                .variable("courseId", course.getId())
                .execute()
                .path("chapters.elements").entityList(Chapter.class).hasSize(2)
                .contains(entitiesToDtos(data.subList(0, 2)))
                .path("chapters.pagination.totalElements").entity(Integer.class).isEqualTo(4)
                .path("chapters.pagination.totalPages").entity(Integer.class).isEqualTo(2)
                .path("chapters.pagination.page").entity(Integer.class).isEqualTo(0)
                .path("chapters.pagination.size").entity(Integer.class).isEqualTo(2)
                .path("chapters.pagination.hasNext").entity(Boolean.class).isEqualTo(true);

        tester.document(query)
                .variable("page", 1)
                .variable("courseId", course.getId())
                .execute()
                .path("chapters.elements").entityList(Chapter.class).hasSize(2)
                .contains(entitiesToDtos(data.subList(2, 4)))
                .path("chapters.pagination.totalElements").entity(Integer.class).isEqualTo(4)
                .path("chapters.pagination.totalPages").entity(Integer.class).isEqualTo(2)
                .path("chapters.pagination.page").entity(Integer.class).isEqualTo(1)
                .path("chapters.pagination.size").entity(Integer.class).isEqualTo(2)
                .path("chapters.pagination.hasNext").entity(Boolean.class).isEqualTo(false);

    }

    /**
     * Given a sort field and a sort direction
     * When querying all chapters
     * Then the chapters are sorted by the given field and direction
     * HINT: Test multiple sort fields in the future
     */
    @Test
    void testGetAllChaptersWithSort(GraphQlTester tester) {
        CourseEntity course = courseRepository.save(dummyCourseBuilder().build());
        var data = Stream.of(
                        dummyChapterBuilder().description("A").courseId(course.getId()).build(),
                        dummyChapterBuilder().description("B").courseId(course.getId()).build(),
                        dummyChapterBuilder().description("C").courseId(course.getId()).build(),
                        dummyChapterBuilder().description("D").courseId(course.getId()).build())
                .map(chapterRepository::save)
                .toList();

        String query = """
                query($courseId: UUID!, $sortDirection: SortDirection!) {
                    chapters(courseId: $courseId, sortBy: "description", sortDirection: [$sortDirection]) {
                        elements {
                            id
                            title
                            description
                            startDate
                            endDate
                            number
                        }
                    }
                }""";

        tester.document(query)
                .variable("sortDirection", SortDirection.ASC)
                .variable("courseId", data.get(0).getCourseId())
                .execute()
                .path("chapters.elements").entityList(Chapter.class).hasSize(4)
                .contains(entitiesToDtos(data));

        tester.document(query)
                .variable("sortDirection", SortDirection.DESC)
                .variable("courseId", data.get(0).getCourseId())
                .execute()
                .path("chapters.elements").entityList(Chapter.class).hasSize(4)
                .contains(entitiesToDtos(List.of(data.get(3), data.get(2), data.get(1), data.get(0))));
    }

    /**
     * Given a filter
     * When querying all chapters
     * Then the chapters are filtered by the given filter
     * HINT: Maybe test more filter fields in the future
     */
    @Test
    void testGetChaptersWithFilter(GraphQlTester tester) {
        CourseEntity course = courseRepository.save(dummyCourseBuilder().build());
        var data = Stream.of(
                        dummyChapterBuilder().title("Chapter 1").description("A").courseId(course.getId()).build(),
                        dummyChapterBuilder().title("Chapter 2").description("B").courseId(course.getId()).build(),
                        dummyChapterBuilder().title("Chapter 3").description("C").courseId(course.getId()).build(),
                        dummyChapterBuilder().title("Chapter 4").description("D").courseId(course.getId()).build())
                .map(chapterRepository::save)
                .toList();

        String query = """
                query($courseId: UUID!, $filter: ChapterFilter!) {
                    chapters(courseId: $courseId, filter: $filter) {
                        elements {
                            id
                            title
                            description
                            startDate
                            endDate
                            number
                        }
                    }
                }""";

        // filter for title contains "Chapter"
        tester.document(query)
                .variable("courseId", data.get(0).getCourseId())
                .variable("filter", ChapterFilter.builder()
                        .setTitle(StringFilter.builder()
                                .setContains("Chapter")
                                .build())
                        .build())
                .execute()
                .path("chapters.elements").entityList(Chapter.class).hasSize(4)
                .contains(entitiesToDtos(data));

        // filter for title contains "Chapter 1"
        tester.document(query)
                .variable("courseId", data.get(0).getCourseId())
                .variable("filter",
                        ChapterFilter.builder()
                                .setTitle(StringFilter.builder()
                                        .setContains("Chapter 1")
                                        .build())
                                .build())
                .execute()
                .path("chapters.elements").entityList(Chapter.class).hasSize(1)
                .contains(entitiesToDtos(List.of(data.get(0))));

        // filter for description contains "A" or "B"
        tester.document(query)
                .variable("courseId", data.get(0).getCourseId())
                .variable("filter",
                        ChapterFilter.builder()
                                .setDescription(StringFilter.builder()
                                        .setContains("A")
                                        .build())
                                .setOr(List.of(
                                        ChapterFilter.builder()
                                                .setDescription(StringFilter.builder()
                                                        .setContains("B")
                                                        .build())
                                                .build()))
                                .build())
                .execute()
                .path("chapters.elements").entityList(Chapter.class).hasSize(2)
                .contains(entitiesToDtos(data.subList(0, 2)));
    }

    private ChapterEntity.ChapterEntityBuilder dummyChapterBuilder() {
        return ChapterEntity.builder()
                .title("Chapter 1")
                .description("This is chapter 1")
                .number(1)
                .startDate(OffsetDateTime.parse("2020-01-01T00:00:00.000Z"))
                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"));
    }

    private CourseEntity.CourseEntityBuilder dummyCourseBuilder() {
        return CourseEntity.builder()
                .title("Course 1")
                .description("This is course 1")
                .published(true)
                .startDate(OffsetDateTime.parse("2020-01-01T00:00:00.000Z"))
                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"));
    }

    private Chapter entityToDto(ChapterEntity entity) {
        var result = modelMapper.map(entity, Chapter.class);
        result.setCourse(null); // we don't check the course here
        return result;
    }

    private Chapter[] entitiesToDtos(List<ChapterEntity> entities) {
        return entities.stream().map(this::entityToDto).toArray(Chapter[]::new);
    }
}
