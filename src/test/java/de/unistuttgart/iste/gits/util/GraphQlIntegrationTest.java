package de.unistuttgart.iste.gits.util;

import org.junit.jupiter.api.extension.*;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

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
public class GraphQlIntegrationTest implements BeforeAllCallback, AfterAllCallback, ParameterResolver {

    private Process dockerProcess;
    private WebTestClient client;
    private Properties applicationProperties;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        applicationProperties = readApplicationProperties();

        stopDockerAndWait();

        dockerProcess = startDocker();
        dockerProcess.waitFor();
        waitForSpringToBeInitialized();

        client = createWebTestClient();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        dockerProcess.destroy();
        stopDockerAndWait();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(GraphQlTester.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return HttpGraphQlTester.create(client);
    }

    /**
     * Waits for the spring application to be initialized.
     * This determined by looking for the log message "Completed initialization in ..."
     * in the docker compose logs.
     */
    private void waitForSpringToBeInitialized() {
        var logProcess = runCommand("docker compose logs -f -n 10 --no-log-prefix");

        try (var logReader = new BufferedReader(new InputStreamReader(logProcess.getInputStream()))) {
            while (true) {
                var line = logReader.readLine();
                System.out.println(line);

                if (line != null && line.contains("Completed initialization in")) {
                    logProcess.destroy();
                    return;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Process runCommand(String command) {
        try {
            return Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void stopDockerAndWait() throws Exception {
        runCommand("docker compose stop").waitFor();
    }

    private Process startDocker() {
        return runCommand("docker compose up -d");
    }

    private WebTestClient createWebTestClient() {
        return WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + applicationProperties.getProperty("dapr.port") + "/graphql")
                // app id is required in header for dapr to route the request to the correct service
                .defaultHeader("dapr-app-id", applicationProperties.getProperty("dapr.appId"))
                .build();
    }

    private Properties readApplicationProperties() {
        var properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/application.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

}
