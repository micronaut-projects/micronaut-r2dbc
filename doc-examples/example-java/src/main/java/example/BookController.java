package example;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.r2dbc.rxjava2.RxConnectionFactory;
import io.reactivex.Flowable;

@Controller("/books")
public class BookController {
    private final RxConnectionFactory connectionFactory;

    public BookController(RxConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    // tag::read[]
    @Get("/")
    Flowable<Book> list() {
        return connectionFactory.withConnection(connection ->
                connection
                    .createStatement("SELECT * FROM BOOKS")
                    .execute()
                    .flatMap(result ->
                        result.map((row, rowMetadata) ->
                                new Book(
                                        row.get(0, String.class),
                                        row.get(1, Integer.class)
                                )
                        )
                    )
        );
    }
    // end::read[]
}
