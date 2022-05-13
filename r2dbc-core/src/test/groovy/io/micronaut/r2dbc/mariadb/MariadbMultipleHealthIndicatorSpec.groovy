package io.micronaut.r2dbc.mariadb

import io.micronaut.context.annotation.Property
import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthResult
import io.micronaut.r2dbc.BasicR2dbcProperties
import io.micronaut.r2dbc.config.R2dbcHealthConfiguration
import io.micronaut.r2dbc.health.R2dbcHealthIndicator
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import io.reactivex.Flowable
import jakarta.inject.Inject
import spock.lang.Shared
import spock.lang.Specification

@MicronautTest
@Property(name = "r2dbc.datasources.my1.url", value = "r2dbc:tc:mariadb:///databasename1?TC_IMAGE_TAG=10.3.6")
@Property(name = "r2dbc.datasources.my2.url", value = "r2dbc:tc:mariadb:///databasename2?TC_IMAGE_TAG=10.3.6")
class MariadbMultipleHealthIndicatorSpec extends Specification {
    @Shared
    @Inject List<R2dbcHealthIndicator> healthIndicators

    void 'test all'() {
        expect:
            healthIndicators.size() == 2
    }

    void 'test health UP'(R2dbcHealthIndicator healthIndicator) {
        given:
        Flowable f = Flowable.fromPublisher(healthIndicator.result)

        when:
        def result = f.map({ HealthResult result ->
            result.status
        }).firstOrError().blockingGet()

        then:
        result == HealthStatus.UP

        where:
        healthIndicator << healthIndicators
    }

}
