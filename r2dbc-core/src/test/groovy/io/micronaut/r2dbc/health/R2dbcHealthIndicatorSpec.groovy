package io.micronaut.r2dbc.health

import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthResult
import io.micronaut.r2dbc.config.R2dbcHealthConfiguration
import io.micronaut.r2dbc.config.R2dbcHealthProperties
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryMetadata
import io.r2dbc.spi.Result
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import io.r2dbc.spi.Statement
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Hooks
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.util.function.BiFunction

class R2dbcHealthIndicatorSpec extends Specification {

    void cleanup() {
        Hooks.resetOnErrorDropped()
    }

    void 'test health result does not drop late execute errors after first result'() {
        given:
        List<Throwable> droppedErrors = []
        Hooks.onErrorDropped(droppedErrors.&add)
        RuntimeException lateError = new RuntimeException('late execute error')
        ConnectionFactory connectionFactory = connectionFactoryFor(Mono.just(connectionFor(emitValueThenError(result(), lateError))))
        R2dbcHealthIndicator healthIndicator = new R2dbcHealthIndicator(connectionFactory, new R2dbcHealthConfiguration())

        when:
        HealthResult result = Mono.from(healthIndicator.result).block()

        then:
        result.status == HealthStatus.UP
        result.details.metadata == 'Oracle Database'
        droppedErrors.empty
    }

    void 'test health result does not drop late create errors after first connection'() {
        given:
        List<Throwable> droppedErrors = []
        Hooks.onErrorDropped(droppedErrors.&add)
        RuntimeException lateError = new RuntimeException('late create error')
        ConnectionFactory connectionFactory = connectionFactoryFor(emitValueThenError(connectionFor(Flux.just(result())), lateError))
        R2dbcHealthIndicator healthIndicator = new R2dbcHealthIndicator(connectionFactory, new R2dbcHealthConfiguration())

        when:
        HealthResult result = Mono.from(healthIndicator.result).block()

        then:
        result.status == HealthStatus.UP
        result.details.metadata == 'Oracle Database'
        droppedErrors.empty
    }

    private ConnectionFactory connectionFactoryFor(Publisher<? extends Connection> connectionPublisher) {
        Stub(ConnectionFactory) {
            create() >> connectionPublisher
            getMetadata() >> Stub(ConnectionFactoryMetadata) {
                getName() >> R2dbcHealthProperties.ORACLE
            }
        }
    }

    private Connection connectionFor(Publisher<? extends Result> executePublisher) {
        Statement statement = Stub(Statement) {
            execute() >> executePublisher
        }
        Stub(Connection) {
            createStatement(R2dbcHealthProperties.ORACLE_QUERY) >> statement
            close() >> Mono.empty()
        }
    }

    private Result result() {
        Row row = Stub(Row) {
            get(0) >> 'Oracle Database'
        }
        RowMetadata metadata = Stub(RowMetadata)
        Stub(Result) {
            map(_ as BiFunction<Row, RowMetadata, Map<String, Object>>) >> { BiFunction<Row, RowMetadata, Map<String, Object>> mappingFunction ->
                Flux.just(mappingFunction.apply(row, metadata))
            }
        }
    }

    private <T> Publisher<T> emitValueThenError(T value, Throwable error) {
        Flux.concat(Mono.just(value), Mono.error(error))
    }
}
