package de.unistuttgart.iste.gits.courseservice;

import org.junit.jupiter.api.Test;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

/**
 * Tests for the GraphQL API. Requires the application to be running.
 * <p>
 * HINT: This is just an example. We need a more sophisticated approach for API testing.
 */
public class GraphQlCourseTest {

    @Test
    public void testCreateCourse() {
        WebTestClient client =
                WebTestClient.bindToServer()
                        .baseUrl("http://localhost:2001/graphql")
                        .build();

        HttpGraphQlTester tester = HttpGraphQlTester.create(client);

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
