package de.unistuttgart.iste.gits.util;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

/**
 * This text extension lets you test the graphQL API with a {@link GraphQlTester}.
 * <p>
 * Usage:
 * <pre>
 *     &#64;ExtendWith(GraphQlIntegrationTestParameterResolver.class)
 *     public class GraphQlTest {
 * </pre>
 * or using the {@link GraphQlApiTest} annotation.
 * <p>
 * In the test methods, the {@link GraphQlTester} can be injected as a parameter.
 * <pre>
 *     &#64;Test
 *     public void test(GraphQlTester tester) {
 *        // ...
 * </pre>
 */
public class GraphQlTesterParameterResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(GraphQlTester.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        WebApplicationContext context = (WebApplicationContext) SpringExtension.getApplicationContext(extensionContext);

        WebTestClient webTestClient = MockMvcWebTestClient.bindToApplicationContext(context)
                .configureClient()
                .baseUrl("/graphql")
                .build();

        return HttpGraphQlTester.create(webTestClient);
    }

}
