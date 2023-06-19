package de.unistuttgart.iste.gits.course_service.integration;

import de.unistuttgart.iste.gits.common.testutil.GitsPostgresSqlContainer;
import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.course_service.persistence.dao.CourseEntity;
import de.unistuttgart.iste.gits.course_service.persistence.dao.CourseResourceAssociationEntity;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.course_service.persistence.repository.ResourceRepository;
import de.unistuttgart.iste.gits.generated.dto.CourseResourceAssociation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@GraphQlApiTest
class QueryResourcesTest {

    @Container
    public static PostgreSQLContainer<GitsPostgresSqlContainer> postgreSQLContainer = GitsPostgresSqlContainer.getInstance();

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    /**
     * Test to check if response when a resource doesn't exist in database
     * @param tester GraphQlTester instance
     */
    @Test
    void testResourceNotExisting(GraphQlTester tester){

        //GraphQL query
        String query = """
                query {
                    resourceById(ids: ["%s"]) {
                        id
                        availableCourses
                        unAvailableCourses
                    }
                }""".formatted(UUID.randomUUID());
        tester.document(query)
                .execute()
                .path("resourceById")
                .entityList(CourseResourceAssociation.class)
                .hasSize(0);
    }

    @Test
    void testGetCourseIdsByResourceId(GraphQlTester tester){

        OffsetDateTime now = OffsetDateTime.now();
        UUID resourceId = UUID.randomUUID();

        // create two courses in the database
        List<CourseEntity> initialCourseData = Stream.of(
                        dummyCourseEntityBuilder(now, true).title("Course 1").build(),
                        dummyCourseEntityBuilder(now,true).title("Course 2").endDate(now.minusMonths(1)).build())
                .map(courseRepository::save)
                .toList();

        //create a resource for each course in the database
        List<CourseResourceAssociationEntity> initialResourceData = Stream.of(
                CourseResourceAssociationEntity.builder()
                        .courseId(initialCourseData.get(0).getId())
                        .resourceId(resourceId)
                        .build(),
                CourseResourceAssociationEntity.builder()
                        .courseId(initialCourseData.get(1).getId())
                        .resourceId(resourceId)
                        .build()
        ).map(resourceRepository::save).toList();

        // expected: two courses share a resource.
        CourseResourceAssociation expected = CourseResourceAssociation.builder()
                .setId(resourceId)
                .setAvailableCourses(List.of(initialCourseData.get(0).getId()))
                .setUnAvailableCourses(List.of(initialCourseData.get(1).getId()))
                .build();


        //GraphQL query
        String query = """
                query {
                    resourceById(ids: ["%s"]) {
                        id
                        availableCourses
                        unAvailableCourses
                    }
                }""".formatted(resourceId);
        tester.document(query)
                .execute()
                .path("resourceById")
                .entityList(CourseResourceAssociation.class)
                .hasSize(1)
                .contains(expected);
    }

    // Builder functions for entities
    private CourseEntity.CourseEntityBuilder dummyCourseEntityBuilder(OffsetDateTime now, boolean published) {
        return CourseEntity.builder()
                .id(UUID.randomUUID())
                .title("title")
                .description("description")
                .startDate(now.minusMonths(2))
                .endDate(now.plusMonths(3))
                .published(published);

    }

}
