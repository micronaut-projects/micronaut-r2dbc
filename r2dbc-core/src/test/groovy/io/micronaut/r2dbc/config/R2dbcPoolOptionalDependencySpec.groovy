package io.micronaut.r2dbc.config

import io.micronaut.context.ApplicationContext
import io.micrometer.core.instrument.binder.MeterBinder
import io.r2dbc.spi.ConnectionFactory
import spock.lang.Specification

class R2dbcPoolOptionalDependencySpec extends Specification {

    void 'application context starts without r2dbc-pool on the application classpath'() {
        given:
        ApplicationContext context

        when:
        context = ApplicationContext.run([
                'r2dbc.datasources.default.url': 'r2dbc:h2:mem:///testdb'
        ])

        then:
        context.getBean(ConnectionFactory) != null
        !context.getBeansOfType(MeterBinder).isEmpty()

        cleanup:
        context?.close()
    }
}
