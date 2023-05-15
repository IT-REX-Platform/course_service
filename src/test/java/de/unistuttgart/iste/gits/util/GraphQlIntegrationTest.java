package de.unistuttgart.iste.gits.util;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.annotation.DirtiesContext;

import java.lang.annotation.*;

/**
 * This annotation can be used to annotate a test class to run it as a graphQL integration test.
 * It will do the following:
 * <ul>
 *     <li>Use a different database than the production database</li>
 *     <li>Drop and recreate the database before and after the test</li>
 *     <li>Inject a {@link GraphQlTester} into the test methods, see {@link GraphQlTestParameterResolver} </li>
 * </ul>
 * <p>
 * Test methods that create or modify data should be annotated with {@link DirtiesContext @DirtiesContext}
 * to ensure that the database is recreated before the next test method is executed.
 */
@ExtendWith(GraphQlTestParameterResolver.class)
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop", // create and drop tables before and after tests
        "spring.datasource.url=jdbc:postgresql://localhost:1032/test_data", // use the test database
})
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface GraphQlIntegrationTest {
}
