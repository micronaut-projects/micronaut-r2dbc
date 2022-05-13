package io.micronaut.r2dbc.oracle

import io.micronaut.context.annotation.Property
import io.micronaut.r2dbc.BasicR2dbcProperties
import io.micronaut.r2dbc.config.R2dbcHealthProperties
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import io.r2dbc.spi.Result
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import io.reactivex.Flowable
import jakarta.inject.Inject
import spock.lang.IgnoreIf
import spock.lang.Specification

import java.util.function.BiFunction

@IgnoreIf({ !jvm.isJava11Compatible() })
@MicronautTest
@Property(name = "r2dbc.datasources.default.url", value = "r2dbc:tc:oracle:///databasename?TC_IMAGE_TAG=10")
@Property(name = "r2dbc.datasources.default.options.applicationName", value = "test")
class OracleConnectionSpec extends Specification {
    @Inject BasicR2dbcProperties props
    @Inject ConnectionFactoryOptions options
    @Inject ConnectionFactory connectionFactory

    void 'test with database URL'() {
        expect:
        props != null
        options.getValue(ConnectionFactoryOptions.DRIVER) == 'tc'
        options.getValue(ConnectionFactoryOptions.DATABASE) == 'databasename'
        options.getValue(ConnectionFactoryOptions.PROTOCOL) == 'oracle'
        options.getValue(Option.valueOf("applicationName")) == 'test'
    }

    void 'test connection'() {
        given:
        Flowable f = Flowable.fromPublisher(connectionFactory.create())

        when:
        def result = f.flatMap({ Connection connection ->
            connection.createStatement(R2dbcHealthProperties.ORACLE_QUERY)
                    .execute()
        }).flatMap({ Result result ->
            result.map({ Row row, RowMetadata rm ->
                row.get(0)
            } as BiFunction)
        }).firstOrError().blockingGet()

        then:
        result
    }
}
