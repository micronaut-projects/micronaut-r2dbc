package io.micronaut.r2dbc.oracle


import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthResult
import io.micronaut.r2dbc.BasicR2dbcProperties
import io.micronaut.r2dbc.health.R2dbcHealthIndicator
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import io.reactivex.Flowable
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class OracleHealthIndicatorSpec extends Specification implements OracleTestPropertyProvider {
    @Inject BasicR2dbcProperties props
    @Inject ConnectionFactoryOptions options
    @Inject ConnectionFactory connectionFactory
    @Inject R2dbcHealthIndicator healthIndicator;

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
