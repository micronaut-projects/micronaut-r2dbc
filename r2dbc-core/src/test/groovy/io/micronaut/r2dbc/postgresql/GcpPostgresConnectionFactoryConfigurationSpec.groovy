package io.micronaut.r2dbc.postgresql

import com.google.cloud.sql.core.CloudSqlConnectionFactory
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.r2dbc.BasicR2dbcProperties
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(rebuildContext = true)
class GcpPostgresConnectionFactoryConfigurationSpec extends Specification {

    @Inject ApplicationContext context

    @Property(name = 'r2dbc.datasources.default.url', value = 'r2dbc:gcp:postgres://db-user:db-pass@project:us-central1:db-instance/appdb')
    void 'test cloud sql url configuration'() {
        given:
        BasicR2dbcProperties props = context.getBean(BasicR2dbcProperties)
        ConnectionFactoryOptions options = context.getBean(ConnectionFactoryOptions)
        ConnectionFactory connectionFactory = context.getBean(ConnectionFactory)

        expect:
        props != null
        options.getValue(ConnectionFactoryOptions.DRIVER) == 'gcp'
        options.getValue(ConnectionFactoryOptions.PROTOCOL) == 'postgres'
        options.getValue(ConnectionFactoryOptions.HOST) == 'project:us-central1:db-instance'
        options.getValue(ConnectionFactoryOptions.DATABASE) == 'appdb'
        options.getValue(ConnectionFactoryOptions.USER) == 'db-user'
        options.getValue(ConnectionFactoryOptions.PASSWORD).toString() == 'db-pass'
        connectionFactory instanceof CloudSqlConnectionFactory
    }

    @Property(name = 'r2dbc.datasources.default.driver', value = 'gcp')
    @Property(name = 'r2dbc.datasources.default.protocol', value = 'postgresql')
    @Property(name = 'r2dbc.datasources.default.host', value = 'project:us-central1:db-instance')
    @Property(name = 'r2dbc.datasources.default.database', value = 'appdb')
    @Property(name = 'r2dbc.datasources.default.username', value = 'db-user')
    @Property(name = 'r2dbc.datasources.default.password', value = 'password')
    @Property(name = 'r2dbc.datasources.default.options.ENABLE_IAM_AUTH', value = 'true')
    void 'test cloud sql properties configuration'() {
        given:
        ConnectionFactoryOptions options = context.getBean(ConnectionFactoryOptions)
        ConnectionFactory connectionFactory = context.getBean(ConnectionFactory)

        expect:
        options.getValue(ConnectionFactoryOptions.DRIVER) == 'gcp'
        options.getValue(ConnectionFactoryOptions.PROTOCOL) == 'postgresql'
        options.getValue(ConnectionFactoryOptions.HOST) == 'project:us-central1:db-instance'
        options.getValue(ConnectionFactoryOptions.DATABASE) == 'appdb'
        options.getValue(ConnectionFactoryOptions.USER) == 'db-user'
        options.getValue(ConnectionFactoryOptions.PASSWORD).toString() == 'password'
        (options.getValue(Option.valueOf('ENABLE_IAM_AUTH')) as Boolean) == true
        connectionFactory instanceof CloudSqlConnectionFactory
    }
}
