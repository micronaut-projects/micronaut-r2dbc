package example

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.r2dbc.rxjava2.RxConnectionFactory
import io.reactivex.Flowable

@Controller("/books")
class BookController {
    private final RxConnectionFactory connectionFactory

    BookController(RxConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory
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
                                            row.get(0, String),
                                            row.get(1, Integer)
                                    )
                            )
                    )
        )
    }
    // end::read[]
}
