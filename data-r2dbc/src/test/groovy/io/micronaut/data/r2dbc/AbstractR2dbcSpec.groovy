package io.micronaut.data.r2dbc

import io.micronaut.data.model.PersistentEntity
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.model.query.builder.sql.SqlQueryBuilder
import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.micronaut.transaction.reactive.ReactiveTransactionStatus
import io.r2dbc.spi.Connection
import reactor.core.publisher.Flux
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

abstract class AbstractR2dbcSpec extends Specification {
    @Shared @Inject R2dbcOperations r2dbcOperations

    def setupSpec() {
        def sqlBuilder = new SqlQueryBuilder(getDialect())
        def statements = getEntities().collect {
            PersistentEntity.of(it)
        }.collect { sqlBuilder.buildBatchCreateTableStatement(it) }
        Flux.from(r2dbcOperations.withTransaction({ ReactiveTransactionStatus<Connection> status ->
            Flux.fromIterable(statements)
                    .flatMap(sql -> {
                        status.connection.createStatement(sql).execute()
                    })
        })).collectList().block()
    }

    protected abstract Dialect getDialect()

    protected abstract List<Class<? extends Object>> getEntities()
}
