package io.micronaut.r2dbc.h2

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.r2dbc.BasicR2dbcProperties
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import spock.lang.Specification

import jakarta.inject.Inject

@MicronautTest(rebuildContext = true)
class H2ConnectionFactoryConfigurationSpec extends Specification {
    @Inject ApplicationContext context

    @Property(name = 'r2dbc.datasources.default.url', value = "r2dbc:h2:mem:///testdb")
    void 'test with database URL'() {
        given:
        BasicR2dbcProperties props = context.getBean(BasicR2dbcProperties)
        ConnectionFactoryOptions options = context.getBean(ConnectionFactoryOptions)

        expect:
        props != null
        options.getValue(ConnectionFactoryOptions.DRIVER) == 'h2'
        options.getValue(ConnectionFactoryOptions.DATABASE) == 'testdb'
        options.getValue(ConnectionFactoryOptions.PROTOCOL) == 'mem'
    }

    @Property(name = 'r2dbc.datasources.default.protocol', value = "mem")
    @Property(name = 'r2dbc.datasources.default.driver', value = "h2")
    @Property(name = 'r2dbc.datasources.default.database', value = "testdb2")
    @Property(name = 'r2dbc.datasources.default.options.DB_CLOSE_DELAY', value = "10")
    void 'test with database props'() {
        given:
        BasicR2dbcProperties props = context.getBean(BasicR2dbcProperties)
        ConnectionFactoryOptions options = context.getBean(ConnectionFactoryOptions)

        expect:
        props != null
        options.getValue(ConnectionFactoryOptions.DRIVER) == 'h2'
        options.getValue(ConnectionFactoryOptions.DATABASE) == 'testdb2'
        options.getValue(ConnectionFactoryOptions.PROTOCOL) == 'mem'
        options.getValue(Option.valueOf('DB_CLOSE_DELAY')) == '10'
    }



}
