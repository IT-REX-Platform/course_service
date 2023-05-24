package de.unistuttgart.iste.gits.courseservice.integration;

import de.unistuttgart.iste.gits.courseservice.persistence.dao.CourseEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.ResourceEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.courseservice.persistence.repository.ResourceRepository;
import de.unistuttgart.iste.gits.generated.dto.CourseIdAvailabilityMapDto;
import de.unistuttgart.iste.gits.generated.dto.ResourceDto;
import de.unistuttgart.iste.gits.util.GraphQlApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@GraphQlApiTest
class QueryResourcesTest {

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
                    courseIdsByResource(ids: ["%s"]) {
                        resource_id
                        courses {
                            course_id
                            available
                        }
                    }
                }""".formatted(UUID.randomUUID());
        tester.document(query)
                .execute()
                .path("courseIdsByResource")
                .entityList(ResourceDto.class)
                .hasSize(0);
    }

    /**
     *
     * @param tester
     */
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
        List<ResourceEntity> initialResourceData = Stream.of(
                dummyResourceEntityBuilder(initialCourseData.get(0).getId(), resourceId).build(),
                dummyResourceEntityBuilder(initialCourseData.get(1).getId(), resourceId).build()
        ).map(resourceRepository::save).toList();

        // expected: two courses share a resource.
        ResourceDto expectedDto = dummyResourceDtoBuilder(
                resourceId,
                List.of(
                        // first has an expired/ faulty date and therefore should not be available
                        new CourseIdAvailabilityMapDto(initialCourseData.get(0).getId(), true),
                        new CourseIdAvailabilityMapDto(initialCourseData.get(1).getId(), false)
                )
        ).build();

        //GraphQL query
        String query = """
                query {
                    courseIdsByResource(ids: ["%s"]) {
                        resource_id
                        courses {
                            course_id
                            available
                        }
                    }
                }""".formatted(resourceId);
        tester.document(query)
                .execute()
                .path("courseIdsByResource")
                .entityList(ResourceDto.class)
                .hasSize(1)
                .contains(expectedDto);
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

    private ResourceEntity.ResourceEntityBuilder dummyResourceEntityBuilder(UUID courseId, UUID resourceId){
        return ResourceEntity.builder().courseId(courseId).resourceId(resourceId);
    }

    private ResourceDto.Builder dummyResourceDtoBuilder(UUID resourceId, List<CourseIdAvailabilityMapDto> courses){
        return ResourceDto.builder().setResource_id(resourceId).setCourses(courses);
    }
}
