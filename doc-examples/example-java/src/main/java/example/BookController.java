package example;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.r2dbc.rxjava2.RxConnectionFactory;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
    Single<Book> create(@Valid Book book) {
        return Single.fromPublisher(bookRepository.save(book));
    }
    // end::create[]

    // tag::read[]
    @Get("/all")
    Flux<Book> all() {
        return Flux.from(bookRepository.findAll());
    }

    @Get("/{id}")
    Mono<Book> show(Long id) {
        return Mono.from(bookRepository.findById(id));
    }
    // end::read[]

    // tag::update[]
    @Put("/{id}")
    Single<Book> update(@NotNull Long id, @Valid Book book) {
        return Single.fromPublisher(bookRepository.update(book));
    }
    // end::update[]

    // tag::delete[]
    @Delete("/{id}")
    Single<HttpResponse<?>> delete(@NotNull Long id) {
        return Single.fromPublisher(bookRepository.deleteById(id))
                .map(deleted -> deleted > 0 ? HttpResponse.noContent() : HttpResponse.notFound());
    }
    // end::delete[]
}
