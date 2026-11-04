package io.micronaut.r2dbc

import io.micronaut.context.ApplicationContext
import io.micronaut.context.BeanLocator
import io.micronaut.context.annotation.Retain
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.core.convert.ConversionService
import io.micronaut.core.io.ResourceLoader
import io.micronaut.core.value.PropertyResolver
import io.micronaut.inject.BeanDefinition
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import spock.lang.Specification

class RetainableConnectionFactorySpec extends Specification {

    void "the connection factory and what it is created from receive nothing bound to the context, so that development mode can retain them"() {
        given:
        ApplicationContext context = ApplicationContext.run([
                'r2dbc.datasources.default.url': 'r2dbc:pool:h2:mem:///retainable;DB_CLOSE_DELAY=-1',
                'r2dbc.datasources.other.url'  : 'r2dbc:h2:mem:///other;DB_CLOSE_DELAY=-1'
        ])

        when:
        Collection<BeanDefinition<?>> factories = context.getBeanDefinitions(ConnectionFactory)
        Collection<BeanDefinition<?>> closure = factories +
                context.getBeanDefinitions(ConnectionFactoryOptions) +
                context.getBeanDefinitions(ConnectionFactoryOptions.Builder) +
                context.getBeanDefinitions(BasicR2dbcProperties) +
                [context.getBeanDefinition(R2dbcConnectionFactoryBean)]

        then:
        factories.size() == 2
        factories.every { it.stringValues(Retain, "invalidatedBy") == ["r2dbc.datasources"] as String[] }
        closure.size() == 9
        closure.every { BeanDefinition<?> definition ->
            !definition.isProxy() && definition.requiredComponents.every { Class<?> type ->
                !BeanLocator.isAssignableFrom(type)
                        && !PropertyResolver.isAssignableFrom(type)
                        && !ApplicationEventPublisher.isAssignableFrom(type)
                        && !ConversionService.isAssignableFrom(type)
                        && !ResourceLoader.isAssignableFrom(type)
            }
        }

        and: 'the URL is still read from the configuration'
        context.getBean(ConnectionFactory).class.name == 'io.r2dbc.pool.ConnectionPool'
        context.getBean(ConnectionFactoryOptions).getValue(ConnectionFactoryOptions.DRIVER) == 'pool'
        context.getBean(ConnectionFactoryOptions).getValue(ConnectionFactoryOptions.PROTOCOL) == 'h2:mem'
        context.getBean(ConnectionFactoryOptions, io.micronaut.inject.qualifiers.Qualifiers.byName('other')).getValue(ConnectionFactoryOptions.DRIVER) == 'h2'

        cleanup:
        context.close()
    }

    void "the properties built from a URL keep the options configured over it"() {
        given:
        ApplicationContext context = ApplicationContext.run([
                'r2dbc.datasources.default.url'     : 'r2dbc:h2:mem:///overridden;DB_CLOSE_DELAY=-1',
                'r2dbc.datasources.default.username': 'override',
                'r2dbc.datasources.default.options.max-size': '7'
        ])

        when:
        ConnectionFactoryOptions options = context.getBean(ConnectionFactoryOptions)

        then:
        options.getValue(ConnectionFactoryOptions.DRIVER) == 'h2'
        options.getValue(ConnectionFactoryOptions.DATABASE) == 'overridden;DB_CLOSE_DELAY=-1'
        options.getValue(ConnectionFactoryOptions.USER) == 'override'
        options.getValue(io.r2dbc.spi.Option.valueOf('maxSize')) == '7'

        cleanup:
        context.close()
    }
}
