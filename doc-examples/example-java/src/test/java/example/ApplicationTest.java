package example;

import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

@MicronautTest
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
