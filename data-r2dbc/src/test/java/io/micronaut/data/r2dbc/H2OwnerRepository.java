package io.micronaut.data.r2dbc;

import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository;
import io.micronaut.data.tck.entities.Owner;

@R2dbcRepository(dialect = Dialect.H2)
interface H2OwnerRepository extends ReactiveStreamsCrudRepository<Owner, Long> {

}