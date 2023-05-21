package de.unistuttgart.iste.gits.util;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.lang.annotation.*;

/**
 * This is an extension for JUnit 5 that starts the application in a docker container
 * with dapr.
 * This will stop the application after the tests are finished.
 * It provides a {@link GraphQlTester} instance that can be used to test
 * the GraphQL API.
 * <p>
 * Usage:
 * <pre>
 *     &#64;ExtendWith(GraphQlIntegrationTest.class)
 *     public class GraphQlTest {
 * </pre>
 * In the test methods, the {@link GraphQlTester} can be injected as a parameter.
 * <pre>
 *     &#64;Test
 *     public void test(GraphQlTester tester) {
 *        // ...
 * </pre>
 */
@ExtendWith(GraphQlTestParameterResolver.class)
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop", // create and drop tables before and after tests
        "spring.datasource.url=jdbc:postgresql://localhost:1033/test_data", // use the test database
})
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface GraphQlApiTest {
}
