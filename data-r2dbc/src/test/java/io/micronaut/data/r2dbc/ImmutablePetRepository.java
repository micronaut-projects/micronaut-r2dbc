package io.micronaut.data.r2dbc;

import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.CrudRepository;

@R2dbcRepository(dialect = Dialect.H2)
public interface ImmutablePetRepository extends CrudRepository<ImmutablePet, Long> {
}
