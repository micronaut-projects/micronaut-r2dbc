package example;

import io.micronaut.context.annotation.Property;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

@MicronautTest
@Property(name = "r2dbc.datasources.default.url", value = "r2dbc:tc:postgresql:///databasename?TC_IMAGE_TAG=9.6.8")
public class ApplicationTest {

    private final EmbeddedApplication<?> application;

    public ApplicationTest(EmbeddedApplication<?> application) {
        this.application = application;
    }

    @Test
    void testRunning() {
        application.isRunning();
    }
}
