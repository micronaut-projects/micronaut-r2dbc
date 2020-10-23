package example;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

@MicronautTest
@Property(name = "r2dbc.datasources.default.url", value = "r2dbc:tc:postgresql:///databasename?TC_IMAGE_TAG=9.6.8")
public class BookControllerTest {

    private final BookClient bookClient;

    public BookControllerTest(BookClient bookClient) {
        this.bookClient = bookClient;
    }

    @Test
    void testListBooks() {
        List<Book> list = bookClient.list();
        Assertions.assertEquals(
                2,
                list.size()
        );
    }

    @Client("/books")
    interface BookClient {
        @Get("/")
        List<Book> list();
    }
}
