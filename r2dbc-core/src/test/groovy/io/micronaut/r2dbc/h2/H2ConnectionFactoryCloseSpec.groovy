package io.micronaut.r2dbc.h2

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import io.micronaut.inject.qualifiers.Qualifiers
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.Closeable
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import jakarta.inject.Named
import jakarta.inject.Singleton
import reactor.core.Disposable
import reactor.core.publisher.Mono
import spock.lang.Specification

class H2ConnectionFactoryCloseSpec extends Specification {

    void 'the connection pool is closed and its connections released when the context stops'() {
        given:
        ApplicationContext context = ApplicationContext.run(
                'r2dbc.datasources.default.url': 'r2dbc:pool:h2:mem:///closedb?maxSize=3'
        )
        ConnectionPool pool = (ConnectionPool) context.getBean(ConnectionFactory)
        useConnection(pool)

        expect:
        !pool.isDisposed()
        pool.metrics.get().allocatedSize() > 0

        when:
        context.close()

        then:
        pool.isDisposed()
        pool.metrics.get().allocatedSize() == 0
        pool.metrics.get().idleSize() == 0
    }

    void 'every data source is closed when the context stops'() {
        given:
        ApplicationContext context = ApplicationContext.run(
                'r2dbc.datasources.default.url': 'r2dbc:pool:h2:mem:///closedb1',
                'r2dbc.datasources.other.url': 'r2dbc:pool:h2:mem:///closedb2',
                'r2dbc.datasources.third.url': 'r2dbc:pool:h2:mem:///closedb3'
        )
        List<ConnectionPool> pools = ['default', 'other', 'third'].collect {
            (ConnectionPool) context.getBean(ConnectionFactory, Qualifiers.byName(it))
        }
        pools.each { useConnection(it) }

        expect:
        pools.unique(false) { System.identityHashCode(it) }.size() == 3
        pools.every { !it.isDisposed() && it.metrics.get().allocatedSize() > 0 }

        when:
        context.close()

        then:
        pools.every { it.isDisposed() && it.metrics.get().allocatedSize() == 0 }
    }

    void 'a connection factory that is not closeable is left alone'() {
        given:
        ApplicationContext context = ApplicationContext.run(
                'r2dbc.datasources.default.url': 'r2dbc:h2:mem:///closedb4',
                'r2dbc.datasources.pooled.url': 'r2dbc:pool:h2:mem:///closedb5'
        )
        ConnectionFactory plain = context.getBean(ConnectionFactory, Qualifiers.byName('default'))
        ConnectionPool pool = (ConnectionPool) context.getBean(ConnectionFactory, Qualifiers.byName('pooled'))

        expect:
        !(plain instanceof Closeable)
        !(plain instanceof Disposable)

        when:
        context.close()

        then:
        noExceptionThrown()
        pool.isDisposed()
    }

    void 'a connection factory that the application creates itself is not closed'() {
        given:
        ApplicationContext context = ApplicationContext.run(
                'spec.name': 'H2ConnectionFactoryCloseSpec',
                'r2dbc.datasources.default.url': 'r2dbc:pool:h2:mem:///closedb6'
        )
        ConnectionPool modulePool = (ConnectionPool) context.getBean(ConnectionFactory, Qualifiers.byName('default'))
        ConnectionPool applicationPool = (ConnectionPool) context.getBean(ConnectionFactory, Qualifiers.byName('application'))

        when:
        context.close()

        then:
        modulePool.isDisposed()
        !applicationPool.isDisposed()

        cleanup:
        applicationPool?.dispose()
    }

    private static void useConnection(ConnectionPool pool) {
        Mono.usingWhen(
                pool.create(),
                { Connection connection -> Mono.from(connection.validate(io.r2dbc.spi.ValidationDepth.REMOTE)) },
                { Connection connection -> connection.close() }
        ).block()
    }

    @Factory
    @Requires(property = 'spec.name', value = 'H2ConnectionFactoryCloseSpec')
    static class ApplicationConnectionFactory {

        @Singleton
        @Named('application')
        ConnectionFactory applicationConnectionFactory() {
            new ConnectionPool(ConnectionPoolConfiguration.builder(
                    ConnectionFactories.get('r2dbc:h2:mem:///closedb7')
            ).build())
        }
    }
}
