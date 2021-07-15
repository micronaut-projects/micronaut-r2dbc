package io.micronaut.r2dbc.mariadb

import io.micronaut.context.annotation.Property
import io.micronaut.r2dbc.BasicR2dbcProperties
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Result
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import io.reactivex.Flowable
import spock.lang.Specification

import jakarta.inject.Inject

@MicronautTest
@Property(name = "r2dbc.datasources.default.url", value = "r2dbc:tc:mariadb:///databasename?TC_IMAGE_TAG=10.3.6")
class MariadbConnectionSpec extends Specification {
    @Inject BasicR2dbcProperties props
    @Inject ConnectionFactoryOptions options
    @Inject ConnectionFactory connectionFactory

    void 'test with database URL'() {
        expect:
        props != null
        options.getValue(ConnectionFactoryOptions.DRIVER) == 'tc'
        options.getValue(ConnectionFactoryOptions.DATABASE) == 'databasename'
        options.getValue(ConnectionFactoryOptions.PROTOCOL) == 'mariadb'
    }

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
