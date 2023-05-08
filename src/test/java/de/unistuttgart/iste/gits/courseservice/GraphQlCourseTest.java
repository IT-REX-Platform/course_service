package de.unistuttgart.iste.gits.courseservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.UUID;

/**
 * Tests for the GraphQL API. Requires the application to be running.
 * <p>
 * HINT: This is just an example. We need a more sophisticated approach for API testing.
 */
@ExtendWith(GraphQlIntegrationTest.class)
public class GraphQlCourseTest {

    @Test
    public void testCreateCourse(GraphQlTester tester) {
        UUID id = tester.documentName("create-course")
                .execute()
                .path("createCourse.title").entity(String.class).isEqualTo("New Course")
                .path("createCourse.description").entity(String.class).isEqualTo("This is a new course")
                .path("createCourse.startDate").entity(String.class).isEqualTo("2020-01-01T00:00:00.000Z")
                .path("createCourse.endDate").entity(String.class).isEqualTo("2021-01-01T00:00:00.000Z")
                .path("createCourse.chapters").entityList(String.class).hasSize(0)
                .path("createCourse.published").entity(Boolean.class).isEqualTo(false)
                .path("createCourse.id").entity(UUID.class).get();

        // cleanup
        tester.documentName("delete-course")
                .variable("uuid", id)
                .execute()
                .path("deleteCourse").entity(UUID.class).isEqualTo(id);
    }
}
