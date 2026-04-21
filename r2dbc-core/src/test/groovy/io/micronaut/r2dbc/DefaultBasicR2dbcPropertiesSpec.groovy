package io.micronaut.r2dbc

import io.micronaut.context.env.Environment
import io.micronaut.core.type.Argument
import io.r2dbc.spi.ConnectionFactoryOptions
import spock.lang.Specification

import java.util.Optional

class DefaultBasicR2dbcPropertiesSpec extends Specification {

    void 'test pooled cloud sql url builder'() {
        given:
        Environment environment = Stub() {
            getProperty('r2dbc.datasources.default.url', Argument.STRING) >> Optional.of('r2dbc:pool:gcp:postgres://db-user:db-pass@project:us-central1:db-instance/appdb')
        }

        DefaultBasicR2dbcProperties properties = new DefaultBasicR2dbcProperties('default', environment)
        ConnectionFactoryOptions options = properties.builder().build()

        expect:
        options.getValue(ConnectionFactoryOptions.DRIVER) == 'pool'
        options.getValue(ConnectionFactoryOptions.PROTOCOL) == 'gcp:postgres'
        options.getValue(ConnectionFactoryOptions.HOST) == 'project:us-central1:db-instance'
        options.getValue(ConnectionFactoryOptions.DATABASE) == 'appdb'
        options.getValue(ConnectionFactoryOptions.USER) == 'db-user'
        options.getValue(ConnectionFactoryOptions.PASSWORD).toString() == 'db-pass'
    }
}
