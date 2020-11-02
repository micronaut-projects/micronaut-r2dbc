package example

import io.micronaut.context.annotation.Property
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import io.micronaut.r2dbc.rxjava2.RxConnectionFactory
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
@Property(name = "r2dbc.datasources.default.url", value = "r2dbc:h2:mem:///testdb")
class BookControllerTest extends Specification {

    @Inject BookClient bookClient

    @Shared @Inject RxConnectionFactory connectionFactory

    def setupSpec() {
        // tag::insert[]
        connectionFactory.withTransaction((connection) -> connection.createBatch()
                .add("CREATE TABLE BOOKS(TITLE VARCHAR(255), PAGES INT)")
                .add("INSERT INTO BOOKS(TITLE, PAGES) VALUES ('The Stand', 1000)")
                .add("INSERT INTO BOOKS(TITLE, PAGES) VALUES ('The Shining', 400)")
                .execute()
        ).blockingSubscribe()
        // end::insert[]
    }

    void "test list books"() {
        when:
        List<Book> list = bookClient.list()

        then:
        list.size() == 2
    }

    @Client("/books")
    static interface BookClient {
        @Get("/")
        List<Book> list()
    }
}
