package de.unistuttgart.iste.gits.courseservice;

import org.junit.jupiter.api.extension.*;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;

public class GraphQlIntegrationTest implements BeforeAllCallback, AfterAllCallback, AfterEachCallback, ParameterResolver {

    private Process daprRun;
    private WebTestClient client;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        daprRun = runCommand(getDaprRunCommand());
        daprRun.getInputStream().transferTo(System.out);
        daprRun.getErrorStream().transferTo(System.err);

        client = WebTestClient.bindToServer()
                        .baseUrl("http://localhost: " + getDaprPortFromProperties() + "/graphql")
                        .build();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        daprRun.destroy();
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {

    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(HttpGraphQlTester.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return HttpGraphQlTester.create(client);
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private Process runCommand(String command) {
        try {
            return Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getDaprRunCommand() {
        if (isWindows()) {
            return "./dapr-run.bat";
        } else {
            return "./dapr-run.sh";
        }
    }

    private String getDaprPortFromProperties() {
        return System.getProperty("dapr.port", "2000");
    }
}
