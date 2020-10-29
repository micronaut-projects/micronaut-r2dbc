package example;

import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.r2dbc.rxjava2.RxConnectionFactory;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BookControllerTest implements TestPropertyProvider {

    @Inject BookClient bookClient;

    @BeforeAll
    static void setupData(RxConnectionFactory connectionFactory) {
        connectionFactory.withTransaction((connection) -> connection.createBatch()
                .add("CREATE TABLE BOOKS(TITLE VARCHAR(255), PAGES INT)")
                .add("INSERT INTO BOOKS(TITLE, PAGES) VALUES ('The Stand', 1000)")
                .add("INSERT INTO BOOKS(TITLE, PAGES) VALUES ('The Shining', 400)")
                .execute()
        ).blockingSubscribe();
    }

    @Test
    void testListBooks() {
        List<Book> list = bookClient.list();
        Assertions.assertEquals(
                2,
                list.size()
        );
    }

    @Override
    public Map<String, String> getProperties() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres").withTag(PostgreSQLContainer.DEFAULT_TAG));
        container.start();
        return CollectionUtils.mapOf(
                "r2dbc.datasources.default.host", container.getHost(),
                "r2dbc.datasources.default.port", container.getFirstMappedPort(),
                "r2dbc.datasources.default.driver", "postgresql",
                "r2dbc.datasources.default.username", container.getUsername(),
                "r2dbc.datasources.default.password", container.getPassword(),
                "r2dbc.datasources.default.database", container.getDatabaseName()
        );
    }

    @Client("/books")
    interface BookClient {
        @Get("/")
        List<Book> list();
    }
}
