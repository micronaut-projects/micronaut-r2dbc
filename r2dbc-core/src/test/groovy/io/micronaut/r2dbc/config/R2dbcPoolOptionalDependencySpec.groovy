package io.micronaut.r2dbc.config

import io.micronaut.context.ApplicationContext
import io.micrometer.core.instrument.binder.MeterBinder
import io.r2dbc.spi.ConnectionFactory
import spock.lang.Specification

class R2dbcPoolOptionalDependencySpec extends Specification {

    void 'application context starts without an explicit r2dbc-pool declaration in the application'() {
        given:
        ApplicationContext context

        when:
        context = ApplicationContext.run([
                'r2dbc.datasources.default.url': 'r2dbc:h2:mem:///testdb'
        ])

        then:
        context.getBean(ConnectionFactory) != null
        context.getBeanDefinitions(MeterBinder)
            .any { it.toString().contains('R2dbcPoolMetricsBinderFactory#r2dbcPoolMeterBinder') }

        cleanup:
        context?.close()
    }
}
