package io.micronaut.data.r2dbc

import io.micronaut.context.annotation.Property
import io.micronaut.data.model.PersistentEntity
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.model.query.builder.sql.SqlQueryBuilder
import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.micronaut.data.tck.entities.Person
import io.micronaut.data.tck.entities.Product
import io.micronaut.data.tck.repositories.PersonReactiveRepository
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.transaction.reactive.ReactiveTransactionStatus
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import reactor.core.publisher.Mono
import spock.lang.Shared

import javax.inject.Inject

@MicronautTest(rollback = false)
@Property(name = "r2dbc.datasources.default.url", value = "r2dbc:tc:sqlserver:///?TC_IMAGE_TAG=2017-CU12")
class MssqlReactiveRepositorySpec extends AbstractReactiveRepositorySpec {
    @Inject
    @Shared
    MssqlPersonRepository personReactiveRepository

    @Inject
    @Shared
    MssqlProductRepository productReactiveRepository

    @Inject
    @Shared
    ConnectionFactory connectionFactory

    @Inject
    @Shared
    R2dbcOperations r2dbcOperations

    @Override
    PersonReactiveRepository getPersonRepository() {
        return personReactiveRepository
    }

    @Override
    ProductReactiveRepository getProductRepository() {
        return productReactiveRepository
    }

    @Override
    void init() {
        def sqlBuilder = new SqlQueryBuilder(Dialect.SQL_SERVER)
        def statement = sqlBuilder.buildBatchCreateTableStatement(PersistentEntity.of(Person), PersistentEntity.of(Product))
        Mono.from(r2dbcOperations.withTransaction({ ReactiveTransactionStatus<Connection> status ->
            status.connection.createStatement(statement).execute()
        })).block()
    }
}
