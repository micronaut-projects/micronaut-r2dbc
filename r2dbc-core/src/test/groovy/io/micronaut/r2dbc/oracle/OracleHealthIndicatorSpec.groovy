package io.micronaut.r2dbc.oracle

import io.micronaut.context.annotation.Property
import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthResult
import io.micronaut.r2dbc.BasicR2dbcProperties
import io.micronaut.r2dbc.health.R2dbcHealthIndicator
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import io.reactivex.Flowable
import jakarta.inject.Inject
import spock.lang.IgnoreIf
import spock.lang.Specification

@IgnoreIf({ !jvm.isJava11Compatible() })
@MicronautTest
@Property(name = "r2dbc.datasources.default.url", value = "r2dbc:tc:oracle:///databasename?TC_IMAGE_TAG=10")
@Property(name = "r2dbc.datasources.default.options.applicationName", value = "test")
class OracleHealthIndicatorSpec extends Specification {
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
