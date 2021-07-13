package io.micronaut.r2dbc.h2

import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.Result
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import io.reactivex.Flowable
import spock.lang.Specification

import jakarta.inject.Inject

@MicronautTest
@Property(name = "r2dbc.datasources.default.url", value = "r2dbc:h2:mem:///testdb")
class H2ConnectionSpec extends Specification {
    @Inject
    ConnectionFactory connectionFactory

    void 'test connection'() {
        given:
        Flowable f = Flowable.fromPublisher(connectionFactory.create())

        when:
        def result = f.flatMap({ Connection connection ->
            connection.createStatement("SELECT 1")
                    .execute()
        }).flatMap({ Result result ->
            result.map({ Row row, RowMetadata rm ->
                row.get(0)
            })
        }).firstOrError().blockingGet()

        then:
        result == 1
    }

}
