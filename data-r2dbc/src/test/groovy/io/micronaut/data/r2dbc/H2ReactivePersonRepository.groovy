package io.micronaut.data.r2dbc

import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.tck.entities.Person
import io.micronaut.data.tck.repositories.PersonReactiveRepository
import io.r2dbc.spi.Connection
import io.reactivex.Single

@R2dbcRepository(dialect = Dialect.H2)
interface H2ReactivePersonRepository extends PersonReactiveRepository {

    Single<Person> findByName(String name, Connection connection)
}
