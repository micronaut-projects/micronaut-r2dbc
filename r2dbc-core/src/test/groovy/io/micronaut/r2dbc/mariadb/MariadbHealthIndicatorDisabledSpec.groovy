package io.micronaut.r2dbc.mariadb

import io.micronaut.context.annotation.Property
import io.micronaut.r2dbc.health.R2dbcHealthIndicator
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import reactor.core.publisher.Flux
import spock.lang.Specification

@MicronautTest
@Property(name = "r2dbc.datasources.default.url", value = "r2dbc:tc:mariadb:///databasename?TC_IMAGE_TAG=10.9.3")
@Property(name = "endpoints.health.r2dbc.database-name-to-health-query.MariaDB", value = "")
class MariadbHealthIndicatorDisabledSpec extends Specification {

    @Inject R2dbcHealthIndicator healthIndicator

    void 'test health disabled'() {
        when:
            Flux f = Flux.from(healthIndicator.result)
        then:
            f.collectList().block().size() == 0
    }
}
