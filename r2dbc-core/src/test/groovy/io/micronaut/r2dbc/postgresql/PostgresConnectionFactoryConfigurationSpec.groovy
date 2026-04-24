package io.micronaut.r2dbc.postgresql

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.r2dbc.BasicR2dbcProperties
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.r2dbc.spi.ConnectionFactoryOptions
import spock.lang.Specification

import jakarta.inject.Inject

@MicronautTest(rebuildContext = true)
class PostgresConnectionFactoryConfigurationSpec extends Specification {
    @Inject ApplicationContext context

    @Property(name = 'r2dbc.datasources.default.url', value = 'r2dbc:postgresql://localhost:2709/mydatabase')
    @Property(name = 'r2dbc.datasources.default.host', value = 'postgres')
    @Property(name = 'r2dbc.datasources.default.port', value = '5432')
    @Property(name = 'r2dbc.datasources.default.username', value = 'user')
    @Property(name = 'r2dbc.datasources.default.password', value = 'secret')
    void 'test host and port override database URL values'() {
        given:
        BasicR2dbcProperties props = context.getBean(BasicR2dbcProperties)
        ConnectionFactoryOptions options = context.getBean(ConnectionFactoryOptions)

        expect:
        props != null
        options.getValue(ConnectionFactoryOptions.DRIVER) == 'postgresql'
        options.getValue(ConnectionFactoryOptions.DATABASE) == 'mydatabase'
        options.getValue(ConnectionFactoryOptions.HOST) == 'postgres'
        options.getValue(ConnectionFactoryOptions.PORT) == 5432
        options.getValue(ConnectionFactoryOptions.USER) == 'user'
        options.getValue(ConnectionFactoryOptions.PASSWORD).toString() == 'secret'
    }
}
