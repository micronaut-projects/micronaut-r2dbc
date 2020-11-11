package example;

import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.r2dbc.operations.R2dbcOperations;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BookControllerTest implements TestPropertyProvider {

    static MySQLContainer<?> container;

    @Inject
    BookClient bookClient;

    @BeforeAll
    static void setupData(R2dbcOperations operations, AuthorRepository authorRepository, BookRepository bookRepository) {
        // tag::programmatic-tx[]
        Mono.from(operations.withTransaction(status ->
            Flux.from(authorRepository.save(new Author("Stephen King")))
                    .flatMap((author -> bookRepository.saveAll(Arrays.asList(
                            new Book("The Stand", 1000, author),
                            new Book("The Shining", 400, author)
                    ))))
            .thenMany(Flux.from(authorRepository.save(new Author("James Patterson"))))
                .flatMap((author ->
                        bookRepository.save(new Book("Along Came a Spider", 300, author))
            )).then()
        )).block();
        // end::programmatic-tx[]
    }

    @AfterAll
    static void cleanup() {
        if (container != null) {
            container.stop();
        }
    }

    @Test
    void testListBooks() {
        List<Book> list = bookClient.list();
        Assertions.assertEquals(
                3,
                list.size()
        );
    }

    @Test
    void testListBooksMicronautData() {
        List<Book> list = bookClient.list();
        Assertions.assertEquals(
                3,
                list.size()
        );
    }

    @Override
    public Map<String, String> getProperties() {
        container = new MySQLContainer<>(DockerImageName.parse("mysql").withTag("5"));
        container.start();
        return CollectionUtils.mapOf(
                "datasources.default.url", container.getJdbcUrl(),
                "datasources.default.username", container.getUsername(),
                "datasources.default.password", container.getPassword(),
                "datasources.default.database", container.getDatabaseName(),
                "r2dbc.datasources.default.host", container.getHost(),
                "r2dbc.datasources.default.port", container.getFirstMappedPort(),
                "r2dbc.datasources.default.driver", "mysql",
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
