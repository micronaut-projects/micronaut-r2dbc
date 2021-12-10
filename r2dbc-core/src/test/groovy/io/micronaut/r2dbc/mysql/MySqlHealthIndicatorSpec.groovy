package io.micronaut.r2dbc.mysql

import io.micronaut.context.annotation.Property
import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthResult
import io.micronaut.r2dbc.BasicR2dbcProperties
import io.micronaut.r2dbc.health.VersionR2dbcHealthIndicator
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import io.reactivex.Flowable
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
@Property(name = "r2dbc.datasources.default.url", value = "r2dbc:tc:mysql:///databasename?TC_IMAGE_TAG=5.7.34&tlsVersion=TLSv1.2")
class MySqlHealthIndicatorSpec extends Specification {
    @Inject BasicR2dbcProperties props
    @Inject ConnectionFactoryOptions options
    @Inject ConnectionFactory connectionFactory
    @Inject VersionR2dbcHealthIndicator healthIndicator;

    void 'test health UP'() {
        given:
        Flowable f = Flowable.fromPublisher(healthIndicator.result)

        when:
        def result = f.map({ HealthResult result ->
            result.status
        }).firstOrError().blockingGet()

        then:
        result == HealthStatus.UP
    }
}
