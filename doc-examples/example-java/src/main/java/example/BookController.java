package example;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.r2dbc.spi.ConnectionFactory;
import io.reactivex.Flowable;

@Controller("/books")
public class BookController {
    private final ConnectionFactory connectionFactory;

    public BookController(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Get("/")
    Flowable<Book> list() {
        return Flowable.fromPublisher(connectionFactory.create())
                .flatMap(connection ->
                    connection.createStatement("SELECT * FROM BOOKS")
                        .execute()
                ).flatMap(result ->
                    result.map((row, rowMetadata) ->
                        new Book(
                                row.get(0, String.class),
                                row.get(1, Integer.class)
                        )
                    )
                );
    }
}
