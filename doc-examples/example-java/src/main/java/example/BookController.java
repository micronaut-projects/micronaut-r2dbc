package example;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.r2dbc.rxjava2.RxConnectionFactory;
import io.reactivex.Flowable;
import io.reactivex.Single;

import javax.inject.Singleton;
import javax.validation.Valid;
import java.util.List;

@Controller("/books")
public class BookController {
    private final RxConnectionFactory connectionFactory;
    private final BookRepository bookRepository;

    public BookController(RxConnectionFactory connectionFactory, BookRepository bookRepository) {
        this.connectionFactory = connectionFactory;
        this.bookRepository = bookRepository;
    }

    // tag::rxfactory[]
    @Get("/")
    Flowable<Book> list() {
        return connectionFactory.withConnection(connection ->
                connection
                    .createStatement("SELECT * FROM BOOK")
                    .execute()
                    .flatMap(result ->
                        result.map((row, rowMetadata) ->
                                new Book(
                                        row.get(1, String.class),
                                        row.get(2, Integer.class)
                                )
                        )
                    )
        );
    }
    // end::rxfactory[]

    // tag::create[]
    @Post("/")
    Single<Book> all(@Valid Book book) {
        return Single.fromPublisher(bookRepository.save(book));
    }
    // end::create[]

    // tag::read[]
    @Get("/all")
    Single<List<Book>> all() {
        return Flowable.fromPublisher(bookRepository.findAll()).toList();
    }
    // end::read[]
}
