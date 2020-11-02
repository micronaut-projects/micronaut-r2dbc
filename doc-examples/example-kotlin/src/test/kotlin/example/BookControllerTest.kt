package example

import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import io.micronaut.r2dbc.rxjava2.RxConnection
import io.micronaut.r2dbc.rxjava2.RxConnectionFactory
import io.micronaut.r2dbc.rxjava2.RxResult
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import io.reactivex.Flowable
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import javax.inject.Inject

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookControllerTest : TestPropertyProvider {

    @Inject
    lateinit var bookClient : BookClient

    @Test
    fun testListBooks() {
        val list = bookClient.list()
        Assertions.assertEquals(
                2,
                list.size
        )
    }

    override fun getProperties(): Map<String, String> {
        val container  = PostgresServer.start()
        return mapOf(
                "r2dbc.datasources.default.host" to container.host,
                "r2dbc.datasources.default.port" to container.firstMappedPort.toString(),
                "r2dbc.datasources.default.driver" to "postgresql",
                "r2dbc.datasources.default.username" to container.username,
                "r2dbc.datasources.default.password" to container.password,
                "r2dbc.datasources.default.database" to container.databaseName
        )
    }

    @Client("/books")
    interface BookClient {
        @Get("/")
        fun list(): List<Book>
    }

    @BeforeAll
    fun setupData(connectionFactory: RxConnectionFactory) {
        // tag::insert[]
        connectionFactory.withTransaction { connection: RxConnection ->
            connection.createBatch()
                    .add("CREATE TABLE BOOKS(TITLE VARCHAR(255), PAGES INT)")
                    .add("INSERT INTO BOOKS(TITLE, PAGES) VALUES ('The Stand', 1000)")
                    .add("INSERT INTO BOOKS(TITLE, PAGES) VALUES ('The Shining', 400)")
                    .execute() as Flowable<RxResult>
        }.blockingSubscribe()
        // end::insert[]
    }
}