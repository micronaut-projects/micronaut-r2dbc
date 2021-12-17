package io.micronaut.r2dbc.config


import io.micronaut.r2dbc.config.R2dbcHealthConfiguration
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(propertySources = "classpath:application-custom-health-query.yml")
class R2dbcHealthConfigurationSpec extends Specification {

    @Inject
    R2dbcHealthConfiguration healthConfiguration;

    void 'custom health query is present'() {
        given:
        String customDatabaseName = "CustomSQL"

        when:
        def result = healthConfiguration.getHealthQuery(customDatabaseName)

        then:
        result.isPresent()
        result.get() == "SELECT custom();"
    }
}
