package io.micronaut.data.r2dbc

import io.micronaut.context.annotation.Property
import io.micronaut.data.model.PersistentEntity
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.model.query.builder.sql.SqlQueryBuilder
import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.micronaut.data.tck.entities.Person
import io.micronaut.data.tck.repositories.PersonReactiveRepository
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.Result
import reactor.core.publisher.Mono
import spock.lang.Shared

import javax.inject.Inject

@MicronautTest(rollback = false)
@Property(name = "r2dbc.datasources.default.url", value = "r2dbc:tc:postgresql:///databasename?TC_IMAGE_TAG=10")
class PostgresReactiveRepositorySpec extends AbstractReactiveRepositorySpec {
    @Inject
    @Shared
    PostgresPersonRepository personReactiveRepository

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
    void init() {
        def sqlBuilder = new SqlQueryBuilder(Dialect.POSTGRES)
        def statement = sqlBuilder.buildBatchCreateTableStatement(PersistentEntity.of(Person))
        Mono.from(r2dbcOperations.withTransaction({ Connection connection ->
            connection.createStatement(statement).execute()
        })).block()
    }
}
