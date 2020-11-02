package example

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.r2dbc.rxjava2.RxConnection
import io.micronaut.r2dbc.rxjava2.RxConnectionFactory
import io.micronaut.r2dbc.rxjava2.RxResult
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import io.reactivex.Flowable

@Controller("/books")
class BookController(private val connectionFactory: RxConnectionFactory) {
    // tag::read[]
    @Get("/")
    fun list(): Flowable<Book> {
        return connectionFactory.withConnection { connection: RxConnection ->
            connection
                .createStatement("SELECT * FROM BOOKS")
                .execute()
                .flatMap { result: RxResult ->
                    result.map { row: Row, rowMetadata: RowMetadata ->
                        Book(
                            row.get(0, String::class.java),
                            row.get(1, Integer::class.java)
                        )
                    }
                }
        }
    }
    // end::read[]
}