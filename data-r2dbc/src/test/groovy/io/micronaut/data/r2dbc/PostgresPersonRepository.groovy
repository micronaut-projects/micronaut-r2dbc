package io.micronaut.data.r2dbc

import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.tck.repositories.PersonReactiveRepository
import io.micronaut.transaction.TransactionDefinition
import io.micronaut.transaction.annotation.TransactionalAdvice

@R2dbcRepository(dialect = Dialect.POSTGRES)
@TransactionalAdvice(isolation = TransactionDefinition.Isolation.SERIALIZABLE)
interface PostgresPersonRepository extends PersonReactiveRepository {
}
